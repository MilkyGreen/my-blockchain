package com.milkygreen.blockchain.vm;

import java.util.ArrayList;
import java.util.List;

import static com.milkygreen.blockchain.vm.TokenType.EOF;
import static com.milkygreen.blockchain.vm.TokenType.SEMICOLON;

import static com.milkygreen.blockchain.vm.TokenType.*;

/**
 */
public class Parser {

    private static class ParseError extends RuntimeException {}

    // 需要处理的token列表
    private final List<Token> tokens;
    // 当前处理到的token位置
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * 对tokens进行解析
     * 采用递归下降的解析方法。对不同的操作划分优先级，每个等级可以解析大于等于自身级别的操作。
     * 比如：equality级别小于comparison，因此equality()会优先交给comparison()解析，之后再尝试自己解析。
     * @return List<Stmt>
     */
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            // 一次解析一个statement
            statements.add(declaration());
        }

        return statements;
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Stmt declaration() {
        try {

            // 其他声明
            return statement();
        } catch (ParseError error) {
            // 如果编译报错，需要跳过当前语句，记录一下错误然后继续编译，尽量一次性抛出更多的编译错误
            synchronize();
            return null;
        }
    }

    /**
     * 处理其他声明
     * @return
     */
    private Stmt statement() {
        return expressionStatement();   // 普通表达式
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        // 从优先级最低的赋值开始解析
        return assignment();
    }

    /**
     * 赋值操作
     * @return
     */
    private Expr assignment() {
        // 先用更高级的OR解析左半边
        Expr expr = or();

        if (match(EQUAL)) {
            // 如果存在 = 说明是一个赋值表达式，继续解析右边

            Token equals = previous();

            // 等号的右边，也是个表达式，用同优先级的assignment解析出来
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                // 左边如果是一个变量，是变量赋值
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                // 左边如果是一个字段的get，则是对象字段的赋值，（set方法其实是左边先出现get，后面跟 = value）
                Expr.Get get = (Expr.Get)expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment target.");
        }else if(match(PLUS_PLUS)){
            // 右边跟的 ++, 是一个 +1操作
            Token line = previous();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, new Expr.Binary(expr,new Token(PLUS,"+",null,line.line),new Expr.Literal(1d)));
            }else{
                error(line, "Invalid ++ target.");
            }
        }else if(match(MINUS_MINUS)){
            // 左边跟的 -- ，是一个-1操作
            Token line = previous();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, new Expr.Binary(expr,new Token(MINUS,"-",null,line.line),new Expr.Literal(1d)));
            }else{
                error(line, "Invalid ++ target.");
            }
        }

        return expr;
    }

    /**
     * 逻辑操作 ||
     * @return
     */
    private Expr or() {
        // todo
        return null;
    }

    /**
     * primary类型表达式指不可再拆分解析的字面token，字符串、数字、true、false、nil、括号 等
     * @return
     */
    private Expr primary() {
        if (match(FALSE)){
            return new Expr.Literal(false);
        }
        if (match(TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(NIL)){
            return new Expr.Literal(null);
        }

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(SUPER)) {
            // super关键字，它后面只能出现方法调用
            Token keyword = previous();
            consume(DOT, "Expect '.' after 'super'.");
            // 解析方法名
            Token method = consume(IDENTIFIER, "Expect superclass method name.");
            // 返回一个父类方法调用表达式
            return new Expr.Super(keyword, method);
        }

        if (match(THIS)){
            // this关键字表达式
            return new Expr.This(previous());
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            // 括号，代表一个处在括号内的表达式，把里面的表达式解析一下，去掉两个括号
            // 括号本身代表了一个最高优先级，因此里面的表达式会被优先解析
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    /**
     * 判断tokenType是否符合预期，如果符合，消费掉这个token
     * @param types 预期类型
     * @return
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()){
            return false;
        }
        return peek().type == type;
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private Token consume(TokenType type, String message) {
        if (check(type)){
            return advance();
        }
        throw error(peek(), message);
    }

    private Token advance() {
        if (!isAtEnd()){
            current++;
        }
        return previous();
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON){
                return;
            }

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
