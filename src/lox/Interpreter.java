package lox;

class Interpreter implements Expr.Visitor<Object> {
    void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object value = evaluate(expr.right);
        switch(expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, value);
                return -(double)value;
            case BANG:
                return !isTruthy(value);
        }
        // Cannot be reached
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        switch(expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left -(double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String) {
                    return left + stringify(right);
                }
                if (right instanceof String) {
                    return stringify(left) + right;
                }
                throw new RuntimeError(expr.operator,
                        "Operands to '+' must be two numbers or include at least one string");
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if ((double)right == 0) {
                    throw new RuntimeError(expr.operator, "Cannot divide by zero.");
                }
                return (double)left / (double)right;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
            case COMMA:
                return right;
        }
        // Cannot be reached
        return null;
    }

    @Override
    public Object visitConditionalExpr(Expr.Conditional expr) {
        Object ifClause = evaluate(expr.condition);
        if (isTruthy(ifClause)) return evaluate(expr.left);
        return evaluate(expr.right);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Double && (double)object == 0) return false;
        if (object instanceof String && object == "") return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand to '" + operator.lexeme + "' must be a number");
    }

    private void checkNumberOperands(Token operator, Object a, Object b) {
        if (a instanceof Double && b instanceof Double) return;
        throw new RuntimeError(operator, "Operands to '" + operator.lexeme + "' must be numbers");
    }

    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length()-2);
            }
            return text;
        }
        return object.toString();
    }
}
