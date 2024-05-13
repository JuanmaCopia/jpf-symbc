package gov.nasa.jpf.symbc.numeric.interp;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.InterpolationContext.ComputeInterpolantResult;

import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
import gov.nasa.jpf.symbc.numeric.SymbolicReal;
import gov.nasa.jpf.symbc.numeric.solvers.ProblemIZ3;

public class SpecialSolverQueries {

    ProblemIZ3 z3;
    SpecialParse parser;

    public SpecialSolverQueries() {
        z3 = new ProblemIZ3();
        parser = new SpecialParse(z3);
    }

    public ComputeInterpolantResult calculateInterpolant(PathCondition pc1, PathCondition pc2) {
        parser.cleanup();
        BoolExpr formulaA = parser.parse(pc1);
        BoolExpr formulaB = parser.parse(pc2);
        return z3.calculateInterpolant(formulaA, formulaB);
    }

    public Set<Expression> extractSymbolicVariables(ComputeInterpolantResult result) {
        Set<Expr> vars = collectInterpolantVariables(result);
        Set<Expression> symVars = obtainSymbolicExpressionsFromMapping(parser, vars);
        return symVars;
    }

//    public Set<Expression> all(PathCondition pc1, PathCondition pc2) {
//        parser.cleanup();
//        BoolExpr formulaA = parser.parse(pc1);
//        BoolExpr formulaB = parser.parse(pc2);
//
//        ComputeInterpolantResult iresult = z3.calculateInterpolant(formulaA, formulaB);
//        Set<Expr> vars = collectInterpolantVariables(iresult);
//
//        BoolExpr[] interpolant = iresult.interp;
//
//        System.err.println("=======================   Interpolant   ======================\n");
//        System.err.println("Formula A: " + formulaA.toString());
//        System.err.println("Formula B: " + formulaB.toString());
//
//        System.err.println("Interpolant: " + Arrays.toString(interpolant));
//
//        System.err.println("Interpolant variables:\n");
//        for (Expr e : vars) {
//            System.err.println("var: " + e.toString());
//        }
//
//        Set<Expression> symVars = obtainSymbolicExpressionsFromMapping(parser, vars);
//        System.err.println("Symbolic variables:\n");
//        for (Expression e : symVars) {
//            System.err.println("symVar: " + e.toString());
//        }
//
//        System.err.println("\n============================================================\n");
//
//        return symVars;
//    }

    Set<Expression> obtainSymbolicExpressionsFromMapping(SpecialParse parser, Set<Expr> dpVars) {
        Set<Expression> symbolicVars = new HashSet<>();

        Map<Object, SymbolicInteger> mappingToSymInt = parser.getDPtoSymbolicIntegerMap();
        Map<Object, SymbolicReal> mappingToSymReal = parser.getDPtoSymbolicRealMap();

        for (Expr dpVar : dpVars) {
            Expression symVar = mappingToSymInt.get(dpVar);
            if (symVar == null)
                symVar = mappingToSymReal.get(dpVar);

            assert symVar != null;
            symbolicVars.add(symVar);
        }

        return symbolicVars;
    }

    public Set<Expr> collectInterpolantVariables(ComputeInterpolantResult iresult) {
        Set<Expr> vars = new HashSet<>();
        for (Expr expr : iresult.interp) {
            collectVariables(expr, vars);
        }
        return vars;
    }

    void collectVariables(Expr expr, Set<Expr> variables) {
        if (expr.isConst()) {
            if (expr.isTrue() || expr.isFalse()) {
                // It's a boolean constant (true or false), not a variable
                return;
            } else if (expr.isNot()) {
                // Recursively collect variables from the negated expression
                for (Expr e : expr.getArgs()) {
                    collectVariables(e, variables);
                }
            } else {
                // It's a boolean variable
                variables.add(expr);
            }
        } else {
            // It's a complex expression, traverse its children
            for (Expr subExpr : expr.getArgs()) {
                collectVariables((Expr) subExpr, variables);
            }
        }
    }

}
