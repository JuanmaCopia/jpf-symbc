package gov.nasa.jpf.symbc.numeric.interp;

import java.util.Arrays;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.InterpolationContext.ComputeInterpolantResult;

import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.solvers.ProblemIZ3;

public class SpecialSolverQueries {

    public static PathCondition calculateInterpolant(PathCondition pc1, PathCondition pc2) {
        ProblemIZ3 z3 = new ProblemIZ3();
        SpecialParse sparser = new SpecialParse(z3);

        BoolExpr formulaA = sparser.parse(pc1);
        BoolExpr formulaB = sparser.parse(pc2);

        ComputeInterpolantResult result = z3.calculateInterpolant(formulaA, formulaB);
        BoolExpr[] interpolant = result.interp;

        System.err.println("=======================   Interpolant   ======================\n");
        System.err.println("Formula A: " + formulaA.toString());
        System.err.println("Formula B: " + formulaB.toString());

        System.err.println("Interpolant: " + Arrays.toString(interpolant));

        System.err.println("\n============================================================\n");
        return null;
    }

}
