package gov.nasa.jpf.symbc.numeric.interp;

import java.util.List;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.InterpolationContext.ComputeInterpolantResult;

import gov.nasa.jpf.symbc.numeric.solvers.ProblemGeneral;

public abstract class InterpolantionSolver extends ProblemGeneral {

	public abstract List<BoolExpr> getConstraints();

	public abstract void clearConstraints();

	public abstract ComputeInterpolantResult calculateInterpolant(BoolExpr A, BoolExpr B);

	public abstract BoolExpr mkAnd(List<BoolExpr> constraints);

}
