package gov.nasa.jpf.symbc.numeric.solvers;

import cvc3.Expr;
import cvc3.Type;
import io.github.cvc5.*;

public class ProblemCVC5 extends ProblemGeneral {
	
	Solver solver;
	
	public ProblemCVC5() {
		solver = new Solver();
		solver.setOption("produce-models", "true");
	    solver.setOption("produce-unsat-cores", "true");
	    try {
			solver.setLogic("ALL");
		} catch (CVC5ApiException e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
		}
	}

	@Override
	public Object makeIntVar(String name, long min, long max) {
		try{
			return solver.mkInteger(name);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object makeRealVar(String name, double min, double max) {
		try{
			return solver.mkReal(name);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object eq(long value, Object exp) {
		try{
			return solver.mkTerm(Kind.EQUAL, solver.mkReal(value), (Term) exp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object eq(Object exp, long value) {
		try{
			return solver.mkTerm(Kind.EQUAL, (Term) exp, solver.mkReal(value));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object eq(Object exp1, Object exp2) {
		try{
			return solver.mkTerm(Kind.EQUAL, (Term) exp1, (Term) exp2);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object eq(double value, Object exp) {
		try{
			return solver.mkTerm(Kind.EQUAL, solver.mkReal(Double.toString(value)), (Term) exp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object eq(Object exp, double value) {
		try{
			return solver.mkTerm(Kind.EQUAL, (Term) exp, solver.mkReal(Double.toString(value)));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object neq(long value, Object exp) {
		try{
			return solver.mkTerm(Kind.NOT, solver.mkTerm(Kind.EQUAL, solver.mkReal(value), (Term) exp));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object neq(Object exp, long value) {
		try{
			return solver.mkTerm(Kind.NOT, solver.mkTerm(Kind.EQUAL, (Term) exp, solver.mkReal(value)));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object neq(Object exp1, Object exp2) {
		try{
			return solver.mkTerm(Kind.NOT, solver.mkTerm(Kind.EQUAL, (Term) exp1, (Term) exp2));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object neq(double value, Object exp) {
		try{
			return solver.mkTerm(Kind.NOT, solver.mkTerm(Kind.EQUAL, solver.mkReal(Double.toString(value)), (Term) exp));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object neq(Object exp, double value) {
		try{
			return solver.mkTerm(Kind.NOT, solver.mkTerm(Kind.EQUAL, (Term) exp, solver.mkReal(Double.toString(value))));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object leq(long value, Object exp) {
		try{
			return solver.mkTerm(Kind.LEQ, solver.mkReal(value), (Term) exp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object leq(Object exp, long value) {
		try{
			return solver.mkTerm(Kind.LEQ, (Term) exp, solver.mkReal(value));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object leq(Object exp1, Object exp2) {
		try{
			return solver.mkTerm(Kind.LEQ, (Term) exp1, (Term) exp2);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object leq(double value, Object exp) {
		try{
			return solver.mkTerm(Kind.LEQ, solver.mkReal(Double.toString(value)), (Term) exp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object leq(Object exp, double value) {
		try{
			return solver.mkTerm(Kind.LEQ, (Term) exp, solver.mkReal(Double.toString(value)));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object geq(long value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object geq(Object exp, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object geq(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object geq(double value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object geq(Object exp, double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object lt(long value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object lt(Object exp, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object lt(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object lt(double value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object lt(Object exp, double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object gt(long value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object gt(Object exp, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object gt(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object gt(double value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object gt(Object exp, double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object plus(long value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object plus(Object exp, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object plus(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object plus(double value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object plus(Object exp, double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object minus(long value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object minus(Object exp, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object minus(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object minus(double value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object minus(Object exp, double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object mult(long value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object mult(Object exp, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object mult(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object mult(double value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object mult(Object exp, double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object div(long value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object div(Object exp, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object div(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object div(double value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object div(Object exp, double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object and(long value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object and(Object exp, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object and(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object or(long value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object or(Object exp, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object or(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object xor(long value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object xor(Object exp, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object xor(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object shiftL(long value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object shiftL(Object exp, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object shiftL(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object shiftR(long value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object shiftR(Object exp, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object shiftR(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object shiftUR(long value, Object exp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object shiftUR(Object exp, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object shiftUR(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object mixed(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean solve() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getRealValueInf(Object dpvar) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getRealValueSup(Object dpVar) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getRealValue(Object dpVar) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getIntValue(Object dpVar) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void post(Object constraint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postLogicalOR(Object[] constraint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object rem(Object exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object rem(long exp1, Object exp2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object rem(Object exp1, long exp2) {
		// TODO Auto-generated method stub
		return null;
	}

}
