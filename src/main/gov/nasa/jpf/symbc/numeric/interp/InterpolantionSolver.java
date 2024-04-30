package gov.nasa.jpf.symbc.numeric.interp;

import com.microsoft.z3.InterpolationContext.ComputeInterpolantResult;

import gov.nasa.jpf.symbc.numeric.solvers.ProblemGeneral;

public abstract class InterpolantionSolver extends ProblemGeneral {

	public abstract Object[] getConstraints();

	public abstract void clearConstraints();

	public abstract ComputeInterpolantResult calculateInterpolant(Object A, Object B);

	public abstract Object mkAnd(Object[] constraints);

}
