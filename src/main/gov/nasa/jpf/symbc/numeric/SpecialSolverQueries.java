package gov.nasa.jpf.symbc.numeric;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.InterpolationContext.ComputeInterpolantResult;

import gov.nasa.jpf.symbc.numeric.interp.SpecialParse;
import gov.nasa.jpf.symbc.numeric.solvers.ProblemIZ3;

public class SpecialSolverQueries {

	public static PathCondition calculateInterpolant(PathCondition pc1, PathCondition pc2) {
		ProblemIZ3 z3 = new ProblemIZ3();
		SpecialParse sparser = new SpecialParse(z3);

		Object formulaA = sparser.parse(pc1);
		Object formulaB = sparser.parse(pc2);

		ComputeInterpolantResult result = z3.calculateInterpolant(formulaA, formulaB);
		BoolExpr[] interpolant = result.interp;

		return null;
	}

}
