package reverse_polish;

import java.util.*;

public class RPCalculator {

    enum States {NEUTRAL, NUM_INT, NUM_DBL, OP, SPEC_OPEN, SPEC_CLOSE, OTHER}

    private Scanner scan;

    private double mem_value;

    public static final String greeting =
            "\n\tReverse Polish Notation Calculator\n" +
                    "\t----------------------------------\n\n"+
                    "\t[operand] [operand] <operator> ...\n\n" +
                    "\tuse [mem] to recall previous result\n" +
                    "\tuse [pi] for pi\n" +
                    "\toperators are [+ - * / % ^]\n";


    public RPCalculator(){
        mem_value = 0;
        scan = new Scanner(System.in);
    }

    public void interactiveCalculator() {

        System.out.println(greeting);

        while(true) {
            try {
                double result = evaluateLine(getTokens());
                mem_value = result;
                System.out.println(result);
            } catch(NoSuchElementException exit){
                System.out.println("\'till next time");
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public double evaluateLine(String line) throws Exception {

        String[] tokens = tokenizeLine(line);

        Stack<Double> stack = new Stack<>();

        double a, b;

        for(String s : tokens) {

            if(isNumber(s)) {
                try {
                    stack.push(Double.parseDouble(s));
                } catch (NumberFormatException e) {
                    throw new NumberFormatException(String.format("Unable to parse \"%s\"", s));
                }
            }
            else if(isOperator(s)) {
                try {
                    b = stack.pop();
                    a = stack.pop();
                } catch (EmptyStackException e) {
                    throw new IllegalStateException("Not enough operands");
                }

                double result;

                switch(s) {
                    case "+":
                        result = a + b; break;
                    case "-":
                        result = a - b; break;
                    case "*":
                        result = a * b; break;
                    case "/":
                        result = a / b; break;
                    case "%":
                        result = a % b; break;
                    case "^":
                        result = Math.pow(a,b); break;
                    default:
                        throw new IllegalArgumentException(
                                String.format("Unknown Operator: \"%s\"", s));
                }

                stack.push(result);
            }
            else if(isSpecial(s)) {

                String special = s.substring(1, s.length()-1).toLowerCase();

                if(special.equals("mem")) {
                    try {
                        stack.push(mem_value);
                    } catch(NullPointerException e) {
                        throw new IllegalArgumentException("No previous value stored in [mem]");
                    }
                }
                else if(special.equals("pi")) {
                    stack.push(Math.PI);
                }
                else {
                    throw new IllegalArgumentException(String.format(
                            "Unsupported special command: \"%s\"", special));
                }

            }
            else if(s.equals("")){
                //ignore random spaces
                continue;
            }
            else {
                throw new IllegalArgumentException(String.format(
                        "Unrecognized token: \"%s\"", s));
            }
        }

        if(stack.size() > 1)
            throw new IllegalStateException(
                    "Too few operators, multiple values still remaining on the stack");

        try{
            return stack.pop();
        } catch(EmptyStackException e){
            return 0;
        }

    }


    private static String[] tokenizeLine(String line) throws IllegalArgumentException {

        String[] strings = line.split(" ");
        StringBuilder sb = new StringBuilder("");
        String errorstring = "Unrecognized token starting with: \"%c\"";

        for(String s : strings) {

            if(isNumber(s) || isOperator(s)) {
                sb.append(s);
                sb.append(" ");
            }
            else {
                States state = States.NEUTRAL;
                char[] chars = s.toCharArray();

                for(char c : chars) {
                    switch(state) {

                        case NEUTRAL:
                            if(isNumber(c)) {
                                sb.append(c);
                                state = States.NUM_INT;
                            }
                            else if(isOperator(c)) {
                                sb.append(c);
                                state = States.OP;
                            }
                            else if(isSpecialOpen(c)) {
                                sb.append(c);
                                state = States.SPEC_OPEN;
                            }
                            else if(isDot(c)) {
                                sb.append(c);
                                state = States.NUM_DBL;
                            }
                            else {
                                throw new IllegalArgumentException(
                                        String.format(errorstring,c));
                            }
                            break;

                        case NUM_INT:
                            if(isSpace(c)) {
                                sb.append(c);
                                state = States.NEUTRAL;
                            }
                            else if(isNumber(c)) {
                                sb.append(c);
                            }
                            else if(isSpecial(c) || isOperator(c)) {
                                sb.append(" ");
                                sb.append(c);
                                state = getState(c);
                            }
                            else if(isDot(c)) {
                                sb.append(c);
                                state = States.NUM_DBL;
                            }
                            else {
                                throw new IllegalArgumentException(
                                        String.format(errorstring,c));
                            }
                            break;

                        case NUM_DBL:
                            if(isSpace(c)) {
                                sb.append(c);
                                state = States.NEUTRAL;
                            }
                            else if(isNumber(c)) {
                                sb.append(c);
                            }
                            else if(isSpecial(c) || isOperator(c)) {
                                sb.append(" ");
                                sb.append(c);
                                state = getState(c);
                            }
                            else {
                                throw new IllegalArgumentException(
                                        String.format(errorstring,c));
                            }
                            break;

                        case OP:
                            if(isSpace(c)) {
                                sb.append(c);
                                state = States.NEUTRAL;
                            }
                            else if(isNumber(c) || isSpecial(c) || isOperator(c)) {
                                sb.append(" ");
                                sb.append(c);
                                state = getState(c);
                            }
                            else if(isDot(c)) {
                                sb.append(" ");
                                sb.append(c);
                                state = States.NUM_DBL;
                            }
                            else {
                                throw new IllegalArgumentException(
                                        String.format(errorstring,c));
                            }
                            break;

                        case SPEC_OPEN:
                            if(isSpecialClose(c)) {
                                sb.append(c);
                                state = States.SPEC_CLOSE;
                            }
                            else {
                                sb.append(c);
                            }
                            break;

                        case SPEC_CLOSE:
                            if(isSpace(c)) {
                                sb.append(c);
                                state = States.NEUTRAL;
                            }
                            else if(isNumber(c) || isSpecial(c) || isOperator(c)) {
                                sb.append(" ");
                                sb.append(c);
                                state = getState(c);
                            }
                            else {
                                throw new IllegalArgumentException(
                                        String.format(errorstring,c));
                            }
                            break;
                        default:
                    }
                }

                sb.append(" ");

            }
        }

        return sb.toString().split(" ");
    }

    private static States getState(char c) {
        if(isSpecialOpen(c))        return States.SPEC_OPEN;
        else if(isSpecialClose(c))  return States.SPEC_CLOSE;
        else if(isNumber(c))        return States.NUM_INT;
        else if(isOperator(c))      return States.OP;
        else                        return States.OTHER;
    }

    private static boolean isSpecial(String s) {
        return !s.equals("") && s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']';
    }

    private static boolean isSpecial(char c) {
        return isSpecial(""+c);
    }

    private static boolean isSpecialOpen(char c) {
        return c == '[';
    }

    private static boolean isSpecialClose(char c) {
        return c == ']';
    }

    private static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e1){
            try {
                Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e2) {
                return false;
            }
        }
    }

    private static boolean isNumber(char c) {
        return isNumber(""+c);
    }

    private static boolean isSpace(char c) {
        return c == ' ';
    }

    private static boolean isDot(char c) {
        return c == '.';
    }

    private static boolean isOperator(String s) {
        String[] ops = {"+","-","*","/","%","^"};

        for(String op : ops)
            if(s.equals(op))
                return true;

        return false;
    }

    private static boolean isOperator(char c) {
        return isOperator(""+c);
    }

    private String getTokens() throws NoSuchElementException {
        System.out.print(">>> ");
        String line = scan.nextLine().trim();
        return line;
    }

    public static void main(String[] args) {

        new RPCalculator().interactiveCalculator();

    }
}

