package gov.nasa.jpf.symbc.numeric.solvers;

import java.math.BigInteger;
import java.util.HashMap;

import io.github.cvc5.Solver;
import io.github.cvc5.Sort;
import io.github.cvc5.Term;
import io.github.cvc5.CVC5ApiException;
import io.github.cvc5.Context;
import io.github.cvc5.Kind;
import io.github.cvc5.Pair;

public class ProblemCVC5 extends ProblemGeneral {
	
	private static class CVC5Wrapper {
		
		private static CVC5Wrapper instance = null;
		
		private Solver solver;
		
		private int numScopes;
		

		public static CVC5Wrapper getInstance() {
			if (instance != null) {
				return instance;
			}
			return instance = new CVC5Wrapper();
		}

		private CVC5Wrapper() {
			solver = new Solver();
			solver.setOption("produce-models", "true");
		    //solver.setOption("produce-unsat-cores", "false");
		    try {
				//solver.setLogic("ALL");
		    	solver.setLogic("QF_LIRA");
			} catch (CVC5ApiException e) {
				e.printStackTrace();
				throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
			}
		}

		public Solver getSolver() {
			return this.solver;
		}
		
		public void push() {
			assert solver != null;
			try {
				solver.push();
			} catch (CVC5ApiException e) {
				throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
			}
			numScopes++;
		}
		
		public void cleanup() {
			if (numScopes > 0) {
				try {
					solver.pop(numScopes);
				} catch (CVC5ApiException e) {
					throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
				}
			}
			numScopes = 0;
		}
	
	}
	
	Solver solver;
	
	public ProblemCVC5() {
		CVC5Wrapper cvc5 = CVC5Wrapper.getInstance();
		cvc5.push();
		solver = cvc5.getSolver();
	}
	
	public void cleanup() {
		CVC5Wrapper.getInstance().cleanup();
	}

	@Override
	public Object makeIntVar(String name, long min, long max) {
		try{
			Sort intSort = solver.getIntegerSort();
			Term intVar = solver.mkConst(intSort, name);
//			solver.assertFormula((Term) geq(intVar, min));
//			solver.assertFormula((Term) leq(intVar, max));
			return intVar;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public Object makeRealVar(String name, double min, double max) {
		try{
			Sort realSort = solver.getRealSort();
			Term realVar = solver.mkConst(realSort, name);
//			solver.assertFormula((Term) geq(realVar, min));
//			solver.assertFormula((Term) leq(realVar, max));
			return realVar;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}
	
	@Override
	public Object makeIntConst(long value) {
	    try{
	    	return solver.mkInteger(value);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}
	
	@Override
    public Object makeRealConst(double value) {
		try{
			return solver.mkReal(Double.toString(value));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
    }
	
	private Term constructTerm(Kind op, long value, Object exp) {
		Term term = (Term) exp;
		try{
			if (term.getSort().equals(solver.getIntegerSort())) {
				return solver.mkTerm(op, solver.mkInteger(value), term);
			} else
				throw new RuntimeException("Term sort is: " + term.getSort());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: " + op + "(long, Object) failed \n" + e);
	    }
	}
	
	private Term constructTerm(Kind op, Object exp, long value) {
		Term term = (Term) exp;
		try{
			if (term.getSort().equals(solver.getIntegerSort())) {
				return solver.mkTerm(op, term, solver.mkInteger(value));
			} else
				throw new RuntimeException("Term sort is: " + term.getSort());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: " + op + "(Object, long) failed \n" + e);
	    }
	}

	private Object constructTerm(Kind op, Object exp1, Object exp2) {
		try{
			return solver.mkTerm(op, (Term) exp1, (Term) exp2);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: " + op + "(Object, Object) failed \n" + e);
	    }
	}
	
	private Object constructTerm(Kind op, double value, Object exp) {
		try{
			return solver.mkTerm(op, solver.mkReal(Double.toString(value)), (Term) exp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: " + op + "(double, Object) failed \n" + e);
	    }
	}
	
	private Object constructTerm(Kind op, Object exp, double value) {
		try{
			return solver.mkTerm(op, (Term) exp, solver.mkReal(Double.toString(value)));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: " + op + "(Object, double) failed \n" + e);
	    }
	}
	
	private Object negateTerm(Object exp) {
		try{
			return solver.mkTerm(Kind.NOT, (Term) exp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: NegateTerm failed \n" + e);
	    }
	}
	
	@Override
	public Object eq(long value, Object exp) {
		return constructTerm(Kind.EQUAL, value, exp);
	}

	@Override
	public Object eq(Object exp, long value) {
		return constructTerm(Kind.EQUAL, exp, value);
	}

	@Override
	public Object eq(Object exp1, Object exp2) {
		return constructTerm(Kind.EQUAL, exp1, exp2);
	}

	@Override
	public Object eq(double value, Object exp) {
		return constructTerm(Kind.EQUAL, value, exp);
	}

	@Override
	public Object eq(Object exp, double value) {
		return constructTerm(Kind.EQUAL, exp, value);
	}
	
	@Override
	public Object neq(long value, Object exp) {
		return negateTerm(eq(value, exp));
	}

	@Override
	public Object neq(Object exp, long value) {
		return negateTerm(eq(exp, value));
	}

	@Override
	public Object neq(Object exp1, Object exp2) {
		return negateTerm(eq(exp1, exp2));
	}

	@Override
	public Object neq(double value, Object exp) {
		return negateTerm(eq(value, exp));
	}

	@Override
	public Object neq(Object exp, double value) {
		return negateTerm(eq(exp, value));
	}

	@Override
	public Object leq(long value, Object exp) {
		return constructTerm(Kind.LEQ, value, exp);
	}

	@Override
	public Object leq(Object exp, long value) {
		return constructTerm(Kind.LEQ, exp, value);
	}

	@Override
	public Object leq(Object exp1, Object exp2) {
		return constructTerm(Kind.LEQ, exp1, exp2);
	}

	@Override
	public Object leq(double value, Object exp) {
		return constructTerm(Kind.LEQ, value, exp);
	}

	@Override
	public Object leq(Object exp, double value) {
		return constructTerm(Kind.LEQ, exp, value);
	}
	
	@Override
	public Object geq(long value, Object exp) {
		return constructTerm(Kind.GEQ, value, exp);
	}

	@Override
	public Object geq(Object exp, long value) {
		return constructTerm(Kind.GEQ, exp, value);
	}

	@Override
	public Object geq(Object exp1, Object exp2) {
		return constructTerm(Kind.GEQ, exp1, exp2);
	}

	@Override
	public Object geq(double value, Object exp) {
		return constructTerm(Kind.GEQ, value, exp);
	}

	@Override
	public Object geq(Object exp, double value) {
		return constructTerm(Kind.GEQ, exp, value);
	}
	
	@Override
	public Object lt(long value, Object exp) {
		return constructTerm(Kind.LT, value, exp);
	}

	@Override
	public Object lt(Object exp, long value) {
		return constructTerm(Kind.LT, exp, value);
	}

	@Override
	public Object lt(Object exp1, Object exp2) {
		return constructTerm(Kind.LT, exp1, exp2);
	}

	@Override
	public Object lt(double value, Object exp) {
		return constructTerm(Kind.LT, value, exp);
	}

	@Override
	public Object lt(Object exp, double value) {
		return constructTerm(Kind.LT, exp, value);
	}
	
	
	@Override
	public Object gt(long value, Object exp) {
		return constructTerm(Kind.GT, value, exp);
	}

	@Override
	public Object gt(Object exp, long value) {
		return constructTerm(Kind.GT, exp, value);
	}

	@Override
	public Object gt(Object exp1, Object exp2) {
		return constructTerm(Kind.GT, exp1, exp2);
	}

	@Override
	public Object gt(double value, Object exp) {
		return constructTerm(Kind.GT, value, exp);
	}

	@Override
	public Object gt(Object exp, double value) {
		return constructTerm(Kind.GT, exp, value);
	}

	@Override
	public Object plus(long value, Object exp) {
		return constructTerm(Kind.ADD, value, exp);
	}

	@Override
	public Object plus(Object exp, long value) {
		return constructTerm(Kind.ADD, exp, value);
	}

	@Override
	public Object plus(Object exp1, Object exp2) {
		return constructTerm(Kind.ADD, exp1, exp2);
	}

	@Override
	public Object plus(double value, Object exp) {
		return constructTerm(Kind.ADD, value, exp);
	}

	@Override
	public Object plus(Object exp, double value) {
		return constructTerm(Kind.ADD, exp, value);
	}
	
	@Override
	public Object minus(long value, Object exp) {
		return constructTerm(Kind.SUB, value, exp);
	}

	@Override
	public Object minus(Object exp, long value) {
		return constructTerm(Kind.SUB, exp, value);
	}

	@Override
	public Object minus(Object exp1, Object exp2) {
		return constructTerm(Kind.SUB, exp1, exp2);
	}

	@Override
	public Object minus(double value, Object exp) {
		return constructTerm(Kind.SUB, value, exp);
	}

	@Override
	public Object minus(Object exp, double value) {
		return constructTerm(Kind.SUB, exp, value);
	}
	
	@Override
	public Object mult(long value, Object exp) {
		return constructTerm(Kind.MULT, value, exp);
	}

	@Override
	public Object mult(Object exp, long value) {
		return constructTerm(Kind.MULT, exp, value);
	}

	@Override
	public Object mult(Object exp1, Object exp2) {
		return constructTerm(Kind.MULT, exp1, exp2);
	}

	@Override
	public Object mult(double value, Object exp) {
		return constructTerm(Kind.MULT, value, exp);
	}

	@Override
	public Object mult(Object exp, double value) {
		return constructTerm(Kind.MULT, exp, value);
	}

	@Override
	public Object div(long value, Object exp) {
		return constructTerm(Kind.INTS_DIVISION, value, exp);
	}

	@Override
	public Object div(Object exp, long value) {
		return constructTerm(Kind.INTS_DIVISION, exp, value);
	}

	@Override
	public Object div(Object exp1, Object exp2) {
		return constructTerm(Kind.INTS_DIVISION, exp1, exp2);
	}

	@Override
	public Object div(double value, Object exp) {
		return constructTerm(Kind.DIVISION, value, exp);
	}

	@Override
	public Object div(Object exp, double value) {
		return constructTerm(Kind.DIVISION, exp, value);
	}
	
	@Override
	public Object and(long value, Object exp) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object and(Object exp, long value) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object and(Object exp1, Object exp2) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}
	
	@Override
	public Object or(long value, Object exp) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object or(Object exp, long value) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object or(Object exp1, Object exp2) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object xor(long value, Object exp) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object xor(Object exp, long value) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object xor(Object exp1, Object exp2) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object shiftL(long value, Object exp) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object shiftL(Object exp, long value) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object shiftL(Object exp1, Object exp2) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object shiftR(long value, Object exp) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object shiftR(Object exp, long value) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object shiftR(Object exp1, Object exp2) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object shiftUR(long value, Object exp) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object shiftUR(Object exp, long value) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}

	@Override
	public Object shiftUR(Object exp1, Object exp2) {
		throw new RuntimeException("## Error not supported, move to CVC5BitVector \n");
	}
	
	@Override
	public Object rem(long value, Object exp) {
		return constructTerm(Kind.INTS_MODULUS, value, exp);
	}

	@Override
	public Object rem(Object exp, long value) {
		return constructTerm(Kind.INTS_MODULUS, exp, value);
	}

	@Override
	public Object rem(Object exp1, Object exp2) {
		return constructTerm(Kind.INTS_MODULUS, exp1, exp2);
	}

	@Override
	public Object mixed(Object exp1, Object exp2) {
		throw new RuntimeException("## Error CVC5: mixed integer/real constraint not yet implemented");
	}

	@Override
	public Boolean solve() {
		try{
			return solver.checkSat().isSat();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}
	
	@Override
	public void post(Object constraint) {
		try{
			solver.assertFormula((Term) constraint);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}
	
	@Override
	public void postLogicalOR(Object[] constraint) {
		post(solver.mkTerm(Kind.OR, (Term[]) constraint));
	}

	@Override
	public double getRealValueInf(Object dpvar) {
		throw new RuntimeException("## Error CVC5: getRealValueInf not implemented");
	}

	@Override
	public double getRealValueSup(Object dpVar) {
		throw new RuntimeException("## Error CVC5: getRealValueSup not implemented");
	}

	@Override
	public double getRealValue(Object dpVar) {
		try{
			Term value = solver.getValue((Term) dpVar);
			Pair<BigInteger, BigInteger> rational = value.getRealValue();
			BigInteger numerator = rational.first;
			BigInteger denominator = rational.second;
			return numerator.doubleValue() / denominator.doubleValue();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

	@Override
	public long getIntValue(Object dpVar) {
		try{
			Term value = solver.getValue((Term) dpVar);
			BigInteger intValue = value.getIntegerValue();
			return intValue.longValue();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
	    }
	}

}
