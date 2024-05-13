package gov.nasa.jpf.symbc.numeric.solvers;

import java.math.BigInteger;

import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import io.github.cvc5.CVC5ApiException;
import io.github.cvc5.Kind;
import io.github.cvc5.Pair;
import io.github.cvc5.Solver;
import io.github.cvc5.Sort;
import io.github.cvc5.Term;

public class ProblemCVC5BitVector extends ProblemGeneral {

	Solver solver;
	Sort bitVectorSort;
	int bitVectorLength;

	public ProblemCVC5BitVector() {
		solver = new Solver();
		// solver.setOption("produce-models", "true");
		// solver.setOption("produce-unsat-cores", "false");
		bitVectorLength = SymbolicInstructionFactory.bvlength;
		try {
			solver.setLogic("QF_BV");
			solver.push();
			bitVectorSort = solver.mkBitVectorSort(bitVectorLength);
		} catch (CVC5ApiException e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
		}
	}

	@Override
	public Object makeIntVar(String name, long min, long max) {
		try {
			Term bvVar = solver.mkConst(bitVectorSort, name);
			return bvVar;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
		}
	}

	@Override
	public Object makeRealVar(String name, double min, double max) {
		try {
			Sort realSort = solver.getRealSort();
			return solver.mkConst(realSort, name);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
		}
	}

	@Override
	public Object makeIntConst(long value) {
		try {
			return solver.mkInteger(value);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
		}
	}

	@Override
	public Object makeRealConst(double value) {
		try {
			return solver.mkReal(Double.toString(value));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
		}
	}

	private Term constructTerm(Kind op, long value, Object exp) {
		Term term = (Term) exp;
		try {
			if (term.getSort().equals(solver.getIntegerSort())) {
				return solver.mkTerm(op, solver.mkInteger(value), term);
			} else if (term.getSort().equals(bitVectorSort)) {
				op = toBitVectorOperator(op);
				return solver.mkTerm(op, solver.mkBitVector(bitVectorLength, value), term);
			} else
				throw new RuntimeException("Term sort is: " + term.getSort());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: " + op + "(long, Object) failed \n" + e);
		}
	}

	private Term constructTerm(Kind op, Object exp, long value) {
		Term term = (Term) exp;
		try {
			if (term.getSort().equals(solver.getIntegerSort())) {
				return solver.mkTerm(op, term, solver.mkInteger(value));
			} else if (term.getSort().equals(bitVectorSort)) {
				op = toBitVectorOperator(op);
				return solver.mkTerm(op, term, solver.mkBitVector(bitVectorLength, value));
			} else
				throw new RuntimeException("Term sort is: " + term.getSort());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: " + op + "(Object, long) failed \n" + e);
		}
	}

	private Object constructTerm(Kind op, Object exp1, Object exp2) {
		try {
			return solver.mkTerm(op, (Term) exp1, (Term) exp2);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: " + op + "(Object, Object) failed \n" + e);
		}
	}

	private Object constructTerm(Kind op, double value, Object exp) {
		try {
			return solver.mkTerm(op, solver.mkReal(Double.toString(value)), (Term) exp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: " + op + "(double, Object) failed \n" + e);
		}
	}

	private Object constructTerm(Kind op, Object exp, double value) {
		try {
			return solver.mkTerm(op, (Term) exp, solver.mkReal(Double.toString(value)));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: " + op + "(Object, double) failed \n" + e);
		}
	}

	private Object negateTerm(Object exp) {
		try {
			return solver.mkTerm(Kind.NOT, (Term) exp);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: NegateTerm failed \n" + e);
		}
	}

	private Kind toBitVectorOperator(Kind op) {
		switch (op) {
			case NOT:
				return Kind.BITVECTOR_NOT;
			case LEQ:
				return Kind.BITVECTOR_SLE;
			case GEQ:
				return Kind.BITVECTOR_SGE;
			case LT:
				return Kind.BITVECTOR_SLT;
			case GT:
				return Kind.BITVECTOR_SGT;
			case ADD:
				return Kind.BITVECTOR_ADD;
			case SUB:
				return Kind.BITVECTOR_SUB;
			case MULT:
				return Kind.BITVECTOR_MULT;
			case DIVISION:
				return Kind.BITVECTOR_SDIV;
			case AND:
				return Kind.BITVECTOR_AND;
			case OR:
				return Kind.BITVECTOR_OR;
			case XOR:
				return Kind.BITVECTOR_XOR;
			default:
				return op;
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
		return constructTerm(Kind.DIVISION, value, exp);
	}

	@Override
	public Object div(Object exp, long value) {
		return constructTerm(Kind.DIVISION, exp, value);
	}

	@Override
	public Object div(Object exp1, Object exp2) {
		return constructTerm(Kind.DIVISION, exp1, exp2);
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
		return constructTerm(Kind.AND, value, exp);
	}

	@Override
	public Object and(Object exp, long value) {
		return constructTerm(Kind.AND, exp, value);
	}

	@Override
	public Object and(Object exp1, Object exp2) {
		return constructTerm(Kind.AND, exp1, exp2);
	}

	@Override
	public Object or(long value, Object exp) {
		return constructTerm(Kind.OR, value, exp);
	}

	@Override
	public Object or(Object exp, long value) {
		return constructTerm(Kind.OR, exp, value);
	}

	@Override
	public Object or(Object exp1, Object exp2) {
		return constructTerm(Kind.OR, exp1, exp2);
	}

	@Override
	public Object xor(long value, Object exp) {
		return constructTerm(Kind.XOR, value, exp);
	}

	@Override
	public Object xor(Object exp, long value) {
		return constructTerm(Kind.XOR, exp, value);
	}

	@Override
	public Object xor(Object exp1, Object exp2) {
		return constructTerm(Kind.XOR, exp1, exp2);
	}

	@Override
	public Object shiftL(long value, Object exp) {
		return constructTerm(Kind.BITVECTOR_SHL, value, exp);
	}

	@Override
	public Object shiftL(Object exp, long value) {
		return constructTerm(Kind.BITVECTOR_SHL, exp, value);
	}

	@Override
	public Object shiftL(Object exp1, Object exp2) {
		return constructTerm(Kind.BITVECTOR_SHL, exp1, exp2);
	}

	@Override
	public Object shiftR(long value, Object exp) {
		return constructTerm(Kind.BITVECTOR_ASHR, value, exp);
	}

	@Override
	public Object shiftR(Object exp, long value) {
		return constructTerm(Kind.BITVECTOR_ASHR, exp, value);
	}

	@Override
	public Object shiftR(Object exp1, Object exp2) {
		return constructTerm(Kind.BITVECTOR_ASHR, exp1, exp2);
	}

	@Override
	public Object shiftUR(long value, Object exp) {
		return constructTerm(Kind.BITVECTOR_LSHR, value, exp);
	}

	@Override
	public Object shiftUR(Object exp, long value) {
		return constructTerm(Kind.BITVECTOR_LSHR, exp, value);
	}

	@Override
	public Object shiftUR(Object exp1, Object exp2) {
		return constructTerm(Kind.BITVECTOR_LSHR, exp1, exp2);
	}

	@Override
	public Object rem(long value, Object exp) {
		return constructTerm(Kind.BITVECTOR_SREM, value, exp);
	}

	@Override
	public Object rem(Object exp, long value) {
		return constructTerm(Kind.BITVECTOR_SREM, exp, value);
	}

	@Override
	public Object rem(Object exp1, Object exp2) {
		return constructTerm(Kind.BITVECTOR_SREM, exp1, exp2);
	}

	@Override
	public Object mixed(Object exp1, Object exp2) {
		throw new RuntimeException("## Error CVC5: mixed integer/real constraint not yet implemented");
	}

	@Override
	public Boolean solve() {
		try {
			return solver.checkSat().isSat();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
		}
	}

	@Override
	public void post(Object constraint) {
		try {
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
		try {
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
		try {
			Term value = solver.getValue((Term) dpVar);
			BigInteger intValue = value.getIntegerValue();
			return intValue.longValue();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error CVC5: Exception caught in CVC5 JNI: \n" + e);
		}
	}

	public void cleanup() {
		try {
			solver.pop();
		} catch (CVC5ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
