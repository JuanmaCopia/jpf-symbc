/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * Symbolic Pathfinder (jpf-symbc) is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
//Copyright (C) 2005 United States Government as represented by the
//Administrator of the National Aeronautics and Space Administration
//(NASA).  All Rights Reserved.
//
//This software is distributed under the NASA Open Source Agreement
//(NOSA), version 1.3.  The NOSA has been approved by the Open Source
//Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
//directory tree for the complete NOSA document.
//
//THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
//KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
//LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
//SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
//A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
//THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
//DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//

package gov.nasa.jpf.symbc.numeric.interp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.z3.BoolExpr;

import gov.nasa.jpf.symbc.arrays.ArrayConstraint;
import gov.nasa.jpf.symbc.arrays.ArrayExpression;
import gov.nasa.jpf.symbc.arrays.InitExpression;
import gov.nasa.jpf.symbc.arrays.RealArrayConstraint;
import gov.nasa.jpf.symbc.arrays.RealStoreExpression;
import gov.nasa.jpf.symbc.arrays.SelectExpression;
import gov.nasa.jpf.symbc.arrays.StoreExpression;
import gov.nasa.jpf.symbc.numeric.BinaryLinearIntegerExpression;
import gov.nasa.jpf.symbc.numeric.BinaryNonLinearIntegerExpression;
import gov.nasa.jpf.symbc.numeric.BinaryRealExpression;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.LinearIntegerConstraint;
import gov.nasa.jpf.symbc.numeric.LogicalORLinearIntegerConstraints;
import gov.nasa.jpf.symbc.numeric.MathFunction;
import gov.nasa.jpf.symbc.numeric.MathRealExpression;
import gov.nasa.jpf.symbc.numeric.MinMax;
import gov.nasa.jpf.symbc.numeric.MixedConstraint;
import gov.nasa.jpf.symbc.numeric.NonLinearIntegerConstraint;
import gov.nasa.jpf.symbc.numeric.Operator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.RealConstant;
import gov.nasa.jpf.symbc.numeric.RealConstraint;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
import gov.nasa.jpf.symbc.numeric.SymbolicReal;

// parses PCs

public class SpecialParse {

    InterpolantionSolver pb;
    public Map<SymbolicReal, Object> symRealToDPVar;
    Map<Object, SymbolicReal> dpVarToSymReal;
    Map<SymbolicInteger, Object> symIntegerToDPVar;
    Map<Object, SymbolicInteger> dpVarToSymInteger;

    int tempVars;

    public SpecialParse(InterpolantionSolver solver) {
        pb = solver;
        symRealToDPVar = new HashMap<SymbolicReal, Object>();
        dpVarToSymReal = new HashMap<Object, SymbolicReal>();
        symIntegerToDPVar = new HashMap<SymbolicInteger, Object>();
        dpVarToSymInteger = new HashMap<Object, SymbolicInteger>();
        tempVars = 0;
    }

    public void cleanup() {
        symRealToDPVar.clear();
        dpVarToSymReal.clear();
        symIntegerToDPVar.clear();
        dpVarToSymInteger.clear();
        tempVars = 0;
    }

    public BoolExpr parse(PathCondition pc) {
        Constraint cRef = pc.header;
        while (cRef != null) {
            if (addConstraint(cRef) == false) {
                throw new RuntimeException("SpecialParser: Parsing error!");
            }
            cRef = cRef.and;
        }

        List<BoolExpr> pcSolverRepresentation = pb.getConstraints();
        pb.clearConstraints();

        return pb.mkAnd(pcSolverRepresentation);
    }

    public Map<SymbolicInteger, Object> getSymbolicIntegerToDPMap() {
        return symIntegerToDPVar;
    }

    public Map<SymbolicReal, Object> getSymbolicRealToDPMap() {
        return symRealToDPVar;
    }

    public Map<Object, SymbolicInteger> getDPtoSymbolicIntegerMap() {
        return dpVarToSymInteger;
    }

    public Map<Object, SymbolicReal> getDPtoSymbolicRealMap() {
        return dpVarToSymReal;
    }

    private boolean addConstraint(Constraint cRef) {
        boolean constraintResult = true;

        if (cRef instanceof RealConstraint)
            constraintResult = createDPRealConstraint((RealConstraint) cRef);// create choco real constraint
        else if (cRef instanceof LinearIntegerConstraint)
            constraintResult = createDPLinearIntegerConstraint((LinearIntegerConstraint) cRef);// create choco linear
                                                                                               // integer constraint
        else if (cRef instanceof MixedConstraint)
            // System.out.println("Mixed Constraint");
            constraintResult = createDPMixedConstraint((MixedConstraint) cRef);
        else if (cRef instanceof LogicalORLinearIntegerConstraints) {
            // if (!(pb instanceof ProblemChoco)) {
            // throw new RuntimeException ("String solving only works with Choco for now");
            // }
            // System.out.println("[SymbolicConstraintsGeneral] reached");
            constraintResult = createDPLinearOrIntegerConstraint((LogicalORLinearIntegerConstraints) cRef);

        } else if (cRef instanceof ArrayConstraint) {
            constraintResult = createArrayConstraint((ArrayConstraint) cRef);
        } else if (cRef instanceof RealArrayConstraint) {
            constraintResult = createRealArrayConstraint((RealArrayConstraint) cRef);
        } else {
            constraintResult = createDPNonLinearIntegerConstraint((NonLinearIntegerConstraint) cRef);
        }
        return constraintResult; // false -> not sat
    }

    // Converts IntegerExpression's into DP's IntExp's
    Object getExpression(IntegerExpression eRef) {
        assert eRef != null;
        assert !(eRef instanceof IntegerConstant);

        if (eRef instanceof SymbolicInteger) {
            Object dp_var = symIntegerToDPVar.get(eRef);
            if (dp_var == null) {
                dp_var = pb.makeIntVar(((SymbolicInteger) eRef).getName(), ((SymbolicInteger) eRef)._min,
                        ((SymbolicInteger) eRef)._max);
                symIntegerToDPVar.put((SymbolicInteger) eRef, dp_var);
                dpVarToSymInteger.put(dp_var, (SymbolicInteger) eRef);
            }
            return dp_var;
        }

        Operator opRef;
        IntegerExpression e_leftRef;
        IntegerExpression e_rightRef;

        if (eRef instanceof BinaryLinearIntegerExpression) {
            opRef = ((BinaryLinearIntegerExpression) eRef).getOp();
            e_leftRef = ((BinaryLinearIntegerExpression) eRef).getLeft();
            e_rightRef = ((BinaryLinearIntegerExpression) eRef).getRight();
        } else { // bin non lin expr
            opRef = ((BinaryNonLinearIntegerExpression) eRef).op;
            e_leftRef = ((BinaryNonLinearIntegerExpression) eRef).left;
            e_rightRef = ((BinaryNonLinearIntegerExpression) eRef).right;
        }
        switch (opRef) {
        case PLUS:
            if (e_leftRef instanceof IntegerConstant && e_rightRef instanceof IntegerConstant)
                throw new RuntimeException("## Error: this is not a symbolic expression"); //
            else if (e_leftRef instanceof IntegerConstant)
                return pb.plus(((IntegerConstant) e_leftRef).value, getExpression(e_rightRef));
            else if (e_rightRef instanceof IntegerConstant)
                return pb.plus(getExpression(e_leftRef), ((IntegerConstant) e_rightRef).value);
            else
                return pb.plus(getExpression(e_leftRef), getExpression(e_rightRef));
        case MINUS:
            if (e_leftRef instanceof IntegerConstant && e_rightRef instanceof IntegerConstant)
                throw new RuntimeException("## Error: this is not a symbolic expression"); //
            else if (e_leftRef instanceof IntegerConstant)
                return pb.minus(((IntegerConstant) e_leftRef).value, getExpression(e_rightRef));
            else if (e_rightRef instanceof IntegerConstant)
                return pb.minus(getExpression(e_leftRef), ((IntegerConstant) e_rightRef).value);
            else
                return pb.minus(getExpression(e_leftRef), getExpression(e_rightRef));
        case MUL:
            if (e_leftRef instanceof IntegerConstant && e_rightRef instanceof IntegerConstant)
                throw new RuntimeException("## Error: this is not a symbolic expression"); //
            else if (e_leftRef instanceof IntegerConstant)
                return pb.mult(((IntegerConstant) e_leftRef).value, getExpression(e_rightRef));
            else if (e_rightRef instanceof IntegerConstant)
                return pb.mult(((IntegerConstant) e_rightRef).value, getExpression(e_leftRef));
            else {
                return pb.mult(getExpression(e_leftRef), getExpression(e_rightRef));
            }
        case DIV:
            if (e_leftRef instanceof IntegerConstant && e_rightRef instanceof IntegerConstant)
                throw new RuntimeException("## Error: this is not a symbolic expression"); //
            else if (e_leftRef instanceof IntegerConstant) // TODO: this might not be linear
                return pb.div(((IntegerConstant) e_leftRef).value, getExpression(e_rightRef));
            else if (e_rightRef instanceof IntegerConstant)
                return pb.div(getExpression(e_leftRef), ((IntegerConstant) e_rightRef).value);
            else {
                return pb.div(getExpression(e_leftRef), getExpression(e_rightRef));
            }
        case REM:
            if (e_leftRef instanceof IntegerConstant && e_rightRef instanceof IntegerConstant)
                throw new RuntimeException("## Error: this is not a symbolic expression"); //
            else if (e_leftRef instanceof IntegerConstant) // TODO: this might not be linear
                return pb.rem(((IntegerConstant) e_leftRef).value, getExpression(e_rightRef));
            else if (e_rightRef instanceof IntegerConstant)
                return pb.rem(getExpression(e_leftRef), ((IntegerConstant) e_rightRef).value);
            else {
                return pb.rem(getExpression(e_leftRef), getExpression(e_rightRef));
            }
        case AND:
            if (e_leftRef instanceof IntegerConstant && e_rightRef instanceof IntegerConstant)
                throw new RuntimeException("## Error: this is not a symbolic expression"); //
            else if (e_leftRef instanceof IntegerConstant)
                return pb.and(((IntegerConstant) e_leftRef).value, getExpression(e_rightRef));
            else if (e_rightRef instanceof IntegerConstant)
                return pb.and(((IntegerConstant) e_rightRef).value, getExpression(e_leftRef));
            else
                return pb.and(getExpression(e_leftRef), getExpression(e_rightRef));
        case OR:
            if (e_leftRef instanceof IntegerConstant && e_rightRef instanceof IntegerConstant)
                throw new RuntimeException("## Error: this is not a symbolic expression"); //
            else if (e_leftRef instanceof IntegerConstant)
                return pb.or(((IntegerConstant) e_leftRef).value, getExpression(e_rightRef));
            else if (e_rightRef instanceof IntegerConstant)
                return pb.or(((IntegerConstant) e_rightRef).value, getExpression(e_leftRef));
            else
                return pb.or(getExpression(e_leftRef), getExpression(e_rightRef));
        case XOR:
            if (e_leftRef instanceof IntegerConstant && e_rightRef instanceof IntegerConstant)
                throw new RuntimeException("## Error: this is not a symbolic expression"); //
            else if (e_leftRef instanceof IntegerConstant)
                return pb.xor(((IntegerConstant) e_leftRef).value, getExpression(e_rightRef));
            else if (e_rightRef instanceof IntegerConstant)
                return pb.xor(((IntegerConstant) e_rightRef).value, getExpression(e_leftRef));
            else
                return pb.xor(getExpression(e_leftRef), getExpression(e_rightRef));
        case SHIFTR:
            if (e_leftRef instanceof IntegerConstant && e_rightRef instanceof IntegerConstant)
                throw new RuntimeException("## Error: this is not a symbolic expression"); //
            else if (e_leftRef instanceof IntegerConstant)
                return pb.shiftR(((IntegerConstant) e_leftRef).value, getExpression(e_rightRef));
            else if (e_rightRef instanceof IntegerConstant)
                return pb.shiftR(getExpression(e_leftRef), ((IntegerConstant) e_rightRef).value);
            else
                return pb.shiftR(getExpression(e_leftRef), getExpression(e_rightRef));
        case SHIFTUR:
            if (e_leftRef instanceof IntegerConstant && e_rightRef instanceof IntegerConstant)
                throw new RuntimeException("## Error: this is not a symbolic expression"); //
            else if (e_leftRef instanceof IntegerConstant)
                return pb.shiftUR(((IntegerConstant) e_leftRef).value, getExpression(e_rightRef));
            else if (e_rightRef instanceof IntegerConstant)
                return pb.shiftUR(getExpression(e_leftRef), ((IntegerConstant) e_rightRef).value);
            else
                return pb.shiftUR(getExpression(e_leftRef), getExpression(e_rightRef));
        case SHIFTL:
            if (e_leftRef instanceof IntegerConstant && e_rightRef instanceof IntegerConstant)
                throw new RuntimeException("## Error: this is not a symbolic expression"); //
            else if (e_leftRef instanceof IntegerConstant)
                return pb.shiftL(((IntegerConstant) e_leftRef).value, getExpression(e_rightRef));
            else if (e_rightRef instanceof IntegerConstant)
                return pb.shiftL(getExpression(e_leftRef), ((IntegerConstant) e_rightRef).value);
            else
                return pb.shiftL(getExpression(e_leftRef), getExpression(e_rightRef));
        default:
            throw new RuntimeException("## Error: Binary Non Linear Operation");
        }

    }

    // Converts RealExpression's into DP RealExp's
    Object getExpression(RealExpression eRef) {
        assert eRef != null;
        assert !(eRef instanceof RealConstant);

        if (eRef instanceof SymbolicReal) {
            Object dp_var = symRealToDPVar.get(eRef);
            if (dp_var == null) {
                dp_var = pb.makeRealVar(((SymbolicReal) eRef).getName(), ((SymbolicReal) eRef)._min,
                        ((SymbolicReal) eRef)._max);
                symRealToDPVar.put((SymbolicReal) eRef, dp_var);
                dpVarToSymReal.put(dp_var, (SymbolicReal) eRef);
            }
            return dp_var;
        }

        if (eRef instanceof BinaryRealExpression) {
            Operator opRef;
            RealExpression e_leftRef;
            RealExpression e_rightRef;
            opRef = ((BinaryRealExpression) eRef).getOp();
            e_leftRef = ((BinaryRealExpression) eRef).getLeft();
            e_rightRef = ((BinaryRealExpression) eRef).getRight();

            switch (opRef) {
            case PLUS:
                if (e_leftRef instanceof RealConstant && e_rightRef instanceof RealConstant)
                    return pb.constant(((RealConstant) e_leftRef).value + ((RealConstant) e_rightRef).value);
                // throw new RuntimeException("## Error: this is not a symbolic expression"); //
                else if (e_leftRef instanceof RealConstant)
                    return pb.plus(((RealConstant) e_leftRef).value, getExpression(e_rightRef));
                else if (e_rightRef instanceof RealConstant)
                    return pb.plus(getExpression(e_leftRef), ((RealConstant) e_rightRef).value);
                else
                    return pb.plus(getExpression(e_leftRef), getExpression(e_rightRef));
            case MINUS:
                if (e_leftRef instanceof RealConstant && e_rightRef instanceof RealConstant)
                    throw new RuntimeException("## Error: this is not a symbolic expression"); //
                else if (e_leftRef instanceof RealConstant)
                    return pb.minus(((RealConstant) e_leftRef).value, getExpression(e_rightRef));
                else if (e_rightRef instanceof RealConstant)
                    return pb.minus(getExpression(e_leftRef), ((RealConstant) e_rightRef).value);
                else
                    return pb.minus(getExpression(e_leftRef), getExpression(e_rightRef));
            case MUL:
                if (e_leftRef instanceof RealConstant && e_rightRef instanceof RealConstant)
                    throw new RuntimeException("## Error: this is not a symbolic expression"); //
                else if (e_leftRef instanceof RealConstant)
                    return pb.mult(((RealConstant) e_leftRef).value, getExpression(e_rightRef));
                else if (e_rightRef instanceof RealConstant)
                    return pb.mult(((RealConstant) e_rightRef).value, getExpression(e_leftRef));
                else
                    return pb.mult(getExpression(e_leftRef), getExpression(e_rightRef));
            case DIV:
                if (e_leftRef instanceof RealConstant && e_rightRef instanceof RealConstant)
                    throw new RuntimeException("## Error: this is not a symbolic expression"); //
                else if (e_leftRef instanceof RealConstant)
                    return pb.div(((RealConstant) e_leftRef).value, getExpression(e_rightRef));
                else if (e_rightRef instanceof RealConstant)
                    return pb.div(getExpression(e_leftRef), ((RealConstant) e_rightRef).value);
                else
                    return pb.div(getExpression(e_leftRef), getExpression(e_rightRef));
            case AND:
                if (e_leftRef instanceof RealConstant && e_rightRef instanceof RealConstant)
                    throw new RuntimeException("## Error: this is not a symbolic expression"); //
                else if (e_leftRef instanceof RealConstant)
                    return pb.and(((RealConstant) e_leftRef).value, getExpression(e_rightRef));
                else if (e_rightRef instanceof RealConstant)
                    return pb.and(((RealConstant) e_rightRef).value, getExpression(e_leftRef));
                else
                    return pb.and(getExpression(e_leftRef), getExpression(e_rightRef));

            default:
                throw new RuntimeException("## Error: Expression " + eRef);
            }
        }

        if (eRef instanceof MathRealExpression) {
            MathFunction funRef;
            RealExpression e_arg1Ref;
            RealExpression e_arg2Ref;

            funRef = ((MathRealExpression) eRef).op;
            e_arg1Ref = ((MathRealExpression) eRef).arg1;
            e_arg2Ref = ((MathRealExpression) eRef).arg2;
            switch (funRef) {
            case SIN:
                return pb.sin(getExpression(e_arg1Ref));
            case COS:
                return pb.cos(getExpression(e_arg1Ref));
            case EXP:
                return pb.exp(getExpression(e_arg1Ref));
            case ASIN:
                return pb.asin(getExpression(e_arg1Ref));
            case ACOS:
                return pb.acos(getExpression(e_arg1Ref));
            case ATAN:
                return pb.atan(getExpression(e_arg1Ref));
            case LOG:
                return pb.log(getExpression(e_arg1Ref));
            case TAN:
                return pb.tan(getExpression(e_arg1Ref));
            case SQRT:
                return pb.sqrt(getExpression(e_arg1Ref));
            case POW:
                if (e_arg2Ref instanceof RealConstant)
                    return pb.power(getExpression(e_arg1Ref), ((RealConstant) e_arg2Ref).value);
                else if (e_arg1Ref instanceof RealConstant)
                    return pb.power(((RealConstant) e_arg1Ref).value, getExpression(e_arg2Ref));
                else
                    return pb.power(getExpression(e_arg1Ref), getExpression(e_arg2Ref));
            case ATAN2:
                if (e_arg2Ref instanceof RealConstant)
                    return pb.atan2(getExpression(e_arg1Ref), ((RealConstant) e_arg2Ref).value);
                else if (e_arg1Ref instanceof RealConstant)
                    return pb.atan2(((RealConstant) e_arg1Ref).value, getExpression(e_arg2Ref));
                else
                    return pb.atan2(getExpression(e_arg1Ref), getExpression(e_arg2Ref));
            default:
                throw new RuntimeException("## Error: Expression " + eRef);
            }
        }

        throw new RuntimeException("## Error: Expression " + eRef);
    }

    public boolean createDPMixedConstraint(MixedConstraint cRef) { // TODO

        Comparator c_compRef = cRef.getComparator();
        RealExpression c_leftRef = (RealExpression) cRef.getLeft();
        IntegerExpression c_rightRef = (IntegerExpression) cRef.getRight();
        assert (c_compRef == Comparator.EQ);

        if (c_leftRef instanceof SymbolicReal && c_rightRef instanceof SymbolicInteger) {
            // pb.post(new
            // MixedEqXY((RealVar)(getExpression(c_leftRef)),(IntDomainVar)(getExpression(c_rightRef))));
            pb.post(pb.mixed(getExpression(c_leftRef), getExpression(c_rightRef)));
        } else if (c_leftRef instanceof SymbolicReal) { // c_rightRef is an IntegerExpression
            Object tmpi = pb.makeIntVar(c_rightRef + "_" + c_rightRef.hashCode(),
                    (int) (((SymbolicReal) c_leftRef)._min), (int) (((SymbolicReal) c_leftRef)._max));
            if (c_rightRef instanceof IntegerConstant)
                pb.post(pb.eq(((IntegerConstant) c_rightRef).value, tmpi));
            else
                pb.post(pb.eq(getExpression(c_rightRef), tmpi));
            // pb.post(new MixedEqXY((RealVar)(getExpression(c_leftRef)),tmpi));
            pb.post(pb.mixed(getExpression(c_leftRef), tmpi));

        } else if (c_rightRef instanceof SymbolicInteger) { // c_leftRef is a RealExpression
            Object tmpr = pb.makeRealVar(c_leftRef + "_" + c_leftRef.hashCode(), ((SymbolicInteger) c_rightRef)._min,
                    ((SymbolicInteger) c_rightRef)._max);
            if (c_leftRef instanceof RealConstant)
                pb.post(pb.eq(tmpr, ((RealConstant) c_leftRef).value));
            else
                pb.post(pb.eq(tmpr, getExpression(c_leftRef)));
            // pb.post(new MixedEqXY(tmpr,(IntDomainVar)(getExpression(c_rightRef))));
            pb.post(pb.mixed(tmpr, getExpression(c_rightRef)));
        } else
            assert (false); // should not be reachable

        return true;
    }

    public boolean createDPRealConstraint(RealConstraint cRef) {

        Comparator c_compRef = cRef.getComparator();
        RealExpression c_leftRef = (RealExpression) cRef.getLeft();
        RealExpression c_rightRef = (RealExpression) cRef.getRight();

        switch (c_compRef) {
        case EQ:
            if (c_leftRef instanceof RealConstant && c_rightRef instanceof RealConstant) {
                if (!(((RealConstant) c_leftRef).value == ((RealConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof RealConstant) {
                pb.post(pb.eq(((RealConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof RealConstant) {
                pb.post(pb.eq(getExpression(c_leftRef), ((RealConstant) c_rightRef).value));
            } else
                pb.post(pb.eq(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case NE:
            if (c_leftRef instanceof RealConstant && c_rightRef instanceof RealConstant) {
                if (!(((RealConstant) c_leftRef).value != ((RealConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof RealConstant) {
                pb.post(pb.neq(((RealConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof RealConstant) {
                pb.post(pb.neq(getExpression(c_leftRef), ((RealConstant) c_rightRef).value));
            } else
                pb.post(pb.neq(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case LT:
            if (c_leftRef instanceof RealConstant && c_rightRef instanceof RealConstant) {
                if (!(((RealConstant) c_leftRef).value < ((RealConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof RealConstant) {
                pb.post(pb.lt(((RealConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof RealConstant) {
                pb.post(pb.lt(getExpression(c_leftRef), ((RealConstant) c_rightRef).value));
            } else
                pb.post(pb.lt(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case GE:
            if (c_leftRef instanceof RealConstant && c_rightRef instanceof RealConstant) {
                if (!(((RealConstant) c_leftRef).value >= ((RealConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof RealConstant) {
                pb.post(pb.geq(((RealConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof RealConstant) {
                pb.post(pb.geq(getExpression(c_leftRef), ((RealConstant) c_rightRef).value));
            } else
                pb.post(pb.geq(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case LE:
            if (c_leftRef instanceof RealConstant && c_rightRef instanceof RealConstant) {
                if (!(((RealConstant) c_leftRef).value <= ((RealConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof RealConstant) {
                pb.post(pb.leq(((RealConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof RealConstant) {
                pb.post(pb.leq(getExpression(c_leftRef), ((RealConstant) c_rightRef).value));
            } else
                pb.post(pb.leq(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case GT:
            if (c_leftRef instanceof RealConstant && c_rightRef instanceof RealConstant) {
                if (!(((RealConstant) c_leftRef).value > ((RealConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof RealConstant) {
                pb.post(pb.gt(((RealConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof RealConstant) {
                pb.post(pb.gt(getExpression(c_leftRef), ((RealConstant) c_rightRef).value));
            } else
                pb.post(pb.gt(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        }
        return true;
    }

    // Added by Gideon, to handle CNF style constraints???
    public boolean createDPLinearOrIntegerConstraint(LogicalORLinearIntegerConstraints c) {
        List<Object> orList = new ArrayList<Object>();

        for (LinearIntegerConstraint cRef : c.getList()) {
            Comparator c_compRef = cRef.getComparator();
            IntegerExpression c_leftRef = (IntegerExpression) cRef.getLeft();
            IntegerExpression c_rightRef = (IntegerExpression) cRef.getRight();
            // Removed all return false: why?
            switch (c_compRef) {
            case EQ:
                if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                    if (((IntegerConstant) c_leftRef).value == ((IntegerConstant) c_rightRef).value)
                        return true;
                } else if (c_leftRef instanceof IntegerConstant) {
                    Object part1 = getExpression(c_rightRef);
                    Object tempVar = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    pb.post(pb.eq(tempVar, part1));
                    Object cc = pb.eq(((IntegerConstant) c_leftRef).value, tempVar);
                    orList.add(cc);
                } else if (c_rightRef instanceof IntegerConstant) {
                    Object part1 = getExpression(c_leftRef);
                    Object tempVar = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    pb.post(pb.eq(tempVar, part1));
                    tempVars++;
                    orList.add(pb.eq(tempVar, ((IntegerConstant) c_rightRef).value));
                } else {
                    Object part1 = getExpression(c_leftRef);
                    Object part2 = getExpression(c_rightRef);
                    Object tempVar1 = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    Object tempVar2 = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    pb.post(pb.eq(tempVar1, part1));
                    pb.post(pb.eq(tempVar2, part2));
                    orList.add(pb.eq(tempVar1, tempVar2));
                }
                break;
            case NE:
                if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                    if (((IntegerConstant) c_leftRef).value != ((IntegerConstant) c_rightRef).value)
                        return true;
                } else if (c_leftRef instanceof IntegerConstant) {
                    Object part1 = getExpression(c_rightRef);
                    Object tempVar = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    pb.post(pb.eq(tempVar, part1));
                    Object cc = pb.neq(((IntegerConstant) c_leftRef).value, tempVar);
                    orList.add(cc);
                } else if (c_rightRef instanceof IntegerConstant) {
                    Object part1 = getExpression(c_leftRef);
                    Object tempVar = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    pb.post(pb.eq(tempVar, part1));
                    tempVars++;
                    orList.add(pb.neq(tempVar, ((IntegerConstant) c_rightRef).value));
                } else {
                    Object part1 = getExpression(c_leftRef);
                    Object part2 = getExpression(c_rightRef);
                    Object tempVar1 = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    Object tempVar2 = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    pb.post(pb.eq(tempVar1, part1));
                    pb.post(pb.eq(tempVar2, part2));
                    orList.add(pb.neq(tempVar1, tempVar2));
                }
                break;
            case LT:
                if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                    if (((IntegerConstant) c_leftRef).value < ((IntegerConstant) c_rightRef).value)
                        return true;
                } else if (c_leftRef instanceof IntegerConstant) {
                    Object part1 = getExpression(c_rightRef);
                    Object tempVar = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    pb.post(pb.eq(tempVar, part1));
                    Object cc = pb.lt(((IntegerConstant) c_leftRef).value, tempVar);
                    orList.add(cc);
                } else if (c_rightRef instanceof IntegerConstant) {
                    Object part1 = getExpression(c_leftRef);
                    Object tempVar = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    pb.post(pb.eq(tempVar, part1));
                    tempVars++;
                    orList.add(pb.lt(tempVar, ((IntegerConstant) c_rightRef).value));
                } else {
                    Object part1 = getExpression(c_leftRef);
                    Object part2 = getExpression(c_rightRef);
                    Object tempVar1 = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    Object tempVar2 = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    pb.post(pb.eq(tempVar1, part1));
                    pb.post(pb.eq(tempVar2, part2));
                    orList.add(pb.lt(tempVar1, tempVar2));
                }
                break;
            case GE:
                if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                    if (((IntegerConstant) c_leftRef).value >= ((IntegerConstant) c_rightRef).value)
                        return true;
                } else if (c_leftRef instanceof IntegerConstant) {
                    Object part1 = getExpression(c_rightRef);
                    Object tempVar = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    pb.post(pb.eq(tempVar, part1));
                    Object cc = pb.geq(((IntegerConstant) c_leftRef).value, tempVar);
                    orList.add(cc);
                } else if (c_rightRef instanceof IntegerConstant) {
                    Object part1 = getExpression(c_leftRef);
                    Object tempVar = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    pb.post(pb.eq(tempVar, part1));
                    tempVars++;
                    orList.add(pb.geq(tempVar, ((IntegerConstant) c_rightRef).value));
                } else {
                    Object part1 = getExpression(c_leftRef);
                    Object part2 = getExpression(c_rightRef);
                    Object tempVar1 = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    Object tempVar2 = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    pb.post(pb.eq(tempVar1, part1));
                    pb.post(pb.eq(tempVar2, part2));
                    orList.add(pb.geq(tempVar1, tempVar2));
                }
                break;
            case LE:
                if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                    if (((IntegerConstant) c_leftRef).value <= ((IntegerConstant) c_rightRef).value)
                        return true;
                } else if (c_leftRef instanceof IntegerConstant) {
                    Object part1 = getExpression(c_rightRef);
                    Object tempVar = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    pb.post(pb.eq(tempVar, part1));
                    Object cc = pb.leq(((IntegerConstant) c_leftRef).value, tempVar);
                    orList.add(cc);
                } else if (c_rightRef instanceof IntegerConstant) {
                    Object part1 = getExpression(c_leftRef);
                    Object tempVar = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    pb.post(pb.eq(tempVar, part1));
                    tempVars++;
                    orList.add(pb.leq(tempVar, ((IntegerConstant) c_rightRef).value));
                } else {
                    Object part1 = getExpression(c_leftRef);
                    Object part2 = getExpression(c_rightRef);
                    Object tempVar1 = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    Object tempVar2 = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    pb.post(pb.eq(tempVar1, part1));
                    pb.post(pb.eq(tempVar2, part2));
                    orList.add(pb.leq(tempVar1, tempVar2));
                }
                break;
            case GT:
                if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                    if (((IntegerConstant) c_leftRef).value > ((IntegerConstant) c_rightRef).value)
                        return true;
                } else if (c_leftRef instanceof IntegerConstant) {
                    Object part1 = getExpression(c_rightRef);
                    Object tempVar = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    pb.post(pb.eq(tempVar, part1));
                    Object cc = pb.gt(((IntegerConstant) c_leftRef).value, tempVar);
                    orList.add(cc);
                } else if (c_rightRef instanceof IntegerConstant) {
                    Object part1 = getExpression(c_leftRef);
                    Object tempVar = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    pb.post(pb.eq(tempVar, part1));
                    tempVars++;
                    orList.add(pb.gt(tempVar, ((IntegerConstant) c_rightRef).value));
                } else {
                    Object part1 = getExpression(c_leftRef);
                    Object part2 = getExpression(c_rightRef);
                    Object tempVar1 = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    Object tempVar2 = pb.makeIntVar("mytemp" + tempVars, MinMax.getVarMinInt(""),
                            MinMax.getVarMaxInt(""));
                    tempVars++;
                    pb.post(pb.eq(tempVar1, part1));
                    pb.post(pb.eq(tempVar2, part2));
                    orList.add(pb.gt(tempVar1, tempVar2));
                }
                break;
            }
        }
        // System.out.println("[SymbolicConstraintsGeneral] orList: " +
        // orList.toString());
        if (orList.size() == 0)
            return true;
        Object constraint_array[] = new Object[orList.size()];
        orList.toArray(constraint_array);

        pb.postLogicalOR(constraint_array);

        return true;

    }

    public boolean createDPLinearIntegerConstraint(LinearIntegerConstraint cRef) {

        Comparator c_compRef = cRef.getComparator();

        IntegerExpression c_leftRef = (IntegerExpression) cRef.getLeft();
        IntegerExpression c_rightRef = (IntegerExpression) cRef.getRight();

        switch (c_compRef) {
        case EQ:
            if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                if (!(((IntegerConstant) c_leftRef).value == ((IntegerConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof IntegerConstant) {
                pb.post(pb.eq(((IntegerConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof IntegerConstant) {
                pb.post(pb.eq(getExpression(c_leftRef), ((IntegerConstant) c_rightRef).value));
            } else
                pb.post(pb.eq(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case NE:
            if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                if (!(((IntegerConstant) c_leftRef).value != ((IntegerConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof IntegerConstant) {
                pb.post(pb.neq(((IntegerConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof IntegerConstant) {
                pb.post(pb.neq(getExpression(c_leftRef), ((IntegerConstant) c_rightRef).value));
            } else
                pb.post(pb.neq(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case LT:
            if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                if (!(((IntegerConstant) c_leftRef).value < ((IntegerConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof IntegerConstant) {
                pb.post(pb.lt(((IntegerConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof IntegerConstant) {
                pb.post(pb.lt(getExpression(c_leftRef), ((IntegerConstant) c_rightRef).value));
            } else
                pb.post(pb.lt(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case GE:
            if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                if (!(((IntegerConstant) c_leftRef).value >= ((IntegerConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof IntegerConstant) {
                pb.post(pb.geq(((IntegerConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof IntegerConstant) {
                pb.post(pb.geq(getExpression(c_leftRef), ((IntegerConstant) c_rightRef).value));
            } else
                pb.post(pb.geq(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case LE:
            if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                if (!(((IntegerConstant) c_leftRef).value <= ((IntegerConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof IntegerConstant) {
                pb.post(pb.leq(((IntegerConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof IntegerConstant) {
                pb.post(pb.leq(getExpression(c_leftRef), ((IntegerConstant) c_rightRef).value));
            } else
                pb.post(pb.leq(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case GT:
            if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                if (!(((IntegerConstant) c_leftRef).value > ((IntegerConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof IntegerConstant) {
                pb.post(pb.gt(((IntegerConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof IntegerConstant) {
                pb.post(pb.gt(getExpression(c_leftRef), ((IntegerConstant) c_rightRef).value));
            } else
                pb.post(pb.gt(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        }
        return true;
    }

    public boolean createDPNonLinearIntegerConstraint(NonLinearIntegerConstraint cRef) {

        Comparator c_compRef = cRef.getComparator();

        IntegerExpression c_leftRef = (IntegerExpression) cRef.getLeft();
        IntegerExpression c_rightRef = (IntegerExpression) cRef.getRight();

        switch (c_compRef) {
        case EQ:
            if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                if (!(((IntegerConstant) c_leftRef).value == ((IntegerConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof IntegerConstant) {
                pb.post(pb.eq(((IntegerConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof IntegerConstant) {
                pb.post(pb.eq(getExpression(c_leftRef), ((IntegerConstant) c_rightRef).value));
            } else
                pb.post(pb.eq(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case NE:
            if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                if (!(((IntegerConstant) c_leftRef).value != ((IntegerConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof IntegerConstant) {
                pb.post(pb.neq(((IntegerConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof IntegerConstant) {
                pb.post(pb.neq(getExpression(c_leftRef), ((IntegerConstant) c_rightRef).value));
            } else
                pb.post(pb.neq(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case LT:
            if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                if (!(((IntegerConstant) c_leftRef).value < ((IntegerConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof IntegerConstant) {
                pb.post(pb.lt(((IntegerConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof IntegerConstant) {
                pb.post(pb.lt(getExpression(c_leftRef), ((IntegerConstant) c_rightRef).value));
            } else
                pb.post(pb.lt(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case GE:
            if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                if (!(((IntegerConstant) c_leftRef).value >= ((IntegerConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof IntegerConstant) {
                pb.post(pb.geq(((IntegerConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof IntegerConstant) {
                pb.post(pb.geq(getExpression(c_leftRef), ((IntegerConstant) c_rightRef).value));
            } else
                pb.post(pb.geq(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case LE:
            if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                if (!(((IntegerConstant) c_leftRef).value <= ((IntegerConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof IntegerConstant) {
                pb.post(pb.leq(((IntegerConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof IntegerConstant) {
                pb.post(pb.leq(getExpression(c_leftRef), ((IntegerConstant) c_rightRef).value));
            } else
                pb.post(pb.leq(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        case GT:
            if (c_leftRef instanceof IntegerConstant && c_rightRef instanceof IntegerConstant) {
                if (!(((IntegerConstant) c_leftRef).value > ((IntegerConstant) c_rightRef).value))
                    return false;
                else
                    return true;
            } else if (c_leftRef instanceof IntegerConstant) {
                pb.post(pb.gt(((IntegerConstant) c_leftRef).value, getExpression(c_rightRef)));
            } else if (c_rightRef instanceof IntegerConstant) {
                pb.post(pb.gt(getExpression(c_leftRef), ((IntegerConstant) c_rightRef).value));
            } else
                pb.post(pb.gt(getExpression(c_leftRef), getExpression(c_rightRef)));
            break;
        }
        return true;
    }
    // static Map<String,Boolean> dpMap = new HashMap<String,Boolean>();

    // Added by Aymeric to support symbolic Arrays
    public boolean createArrayConstraint(ArrayConstraint cRef) {
        Comparator c_compRef = cRef.getComparator();

        SelectExpression selex = null;
        StoreExpression stoex = null;
        IntegerExpression sel_right = null;
        ArrayExpression sto_right = null;
        InitExpression initex = null;
        if (cRef.getLeft() instanceof SelectExpression) {
            selex = (SelectExpression) cRef.getLeft();
            sel_right = (IntegerExpression) cRef.getRight();
        } else if (cRef.getLeft() instanceof StoreExpression) {
            stoex = (StoreExpression) cRef.getLeft();
            sto_right = (ArrayExpression) cRef.getRight();
        } else if (cRef.getLeft() instanceof InitExpression) {
            initex = (InitExpression) cRef.getLeft();
        } else {
            throw new RuntimeException("ArrayConstraint is not select or store or init");
        }

        switch (c_compRef) {
        case EQ:

            if (selex != null && sel_right != null) {
                // The array constraint is a select
                ArrayExpression ae = (ArrayExpression) selex.arrayExpression;
                pb.post(pb.eq(
                        pb.select(pb.makeArrayVar(ae.getName()),
                                (selex.indexExpression instanceof IntegerConstant)
                                        ? pb.makeIntConst(((IntegerConstant) selex.indexExpression).value)
                                        : getExpression(selex.indexExpression)),
                        (sel_right instanceof IntegerConstant) ? pb.makeIntConst(((IntegerConstant) sel_right).value)
                                : getExpression(sel_right)));
                break;
            }
            if (stoex != null && sto_right != null) {
                // The array constraint is a store
                ArrayExpression ae = (ArrayExpression) stoex.arrayExpression;
                ArrayExpression newae = (ArrayExpression) sto_right;
                pb.post(pb.eq(pb.store(pb.makeArrayVar(ae.getName()),
                        (stoex.indexExpression instanceof IntegerConstant)
                                ? pb.makeIntConst(((IntegerConstant) stoex.indexExpression).value)
                                : getExpression(stoex.indexExpression),
                        (stoex.value instanceof IntegerConstant)
                                ? pb.makeIntConst(((IntegerConstant) stoex.value).value)
                                : getExpression(stoex.value)),
                        pb.makeArrayVar(newae.getName())));
                break;
            }
            if (initex != null) {
                // The array constraint is an initialization
                ArrayExpression ae = (ArrayExpression) initex.arrayExpression;
                IntegerConstant init_value = new IntegerConstant(initex.isRef ? -1 : 0);
                pb.post(pb.init_array(pb.makeArrayVar(ae.getName()), pb.makeIntConst(init_value.value)));
                break;
            }
            throw new RuntimeException("ArrayConstraint is not correct select or store or init");
        case NE:
            if (selex != null && sel_right != null) {
                // The array constraint is a select
                ArrayExpression ae = (ArrayExpression) selex.arrayExpression;
                pb.post(pb.neq(pb.select(pb.makeArrayVar(ae.getName()), getExpression(selex.indexExpression)),
                        getExpression(sel_right)));
                break;
            }
            if (stoex != null && sto_right != null) {
                // The array constraint is a store
                ArrayExpression ae = (ArrayExpression) stoex.arrayExpression;
                ArrayExpression newae = (ArrayExpression) sto_right;
                pb.post(pb.neq(pb.store(pb.makeArrayVar(ae.getName()), getExpression(stoex.indexExpression),
                        getExpression(stoex.value)), newae));
                break;
            }
            throw new RuntimeException("ArrayConstraint is not correct select or store");
        default:
            throw new RuntimeException("ArrayConstraint is not select or store");
        }
        return true;
    }

    public boolean createRealArrayConstraint(final RealArrayConstraint cRef) {
        final Comparator c_compRef = cRef.getComparator();

        SelectExpression selex = null;
        RealStoreExpression stoex = null;
        RealExpression sel_right = null;
        ArrayExpression sto_right = null;
        if (cRef.getLeft() instanceof SelectExpression) {
            selex = (SelectExpression) cRef.getLeft();
            sel_right = (RealExpression) cRef.getRight();
        } else if (cRef.getLeft() instanceof RealStoreExpression) {
            stoex = (RealStoreExpression) cRef.getLeft();
            sto_right = (ArrayExpression) cRef.getRight();
        } else {
            throw new RuntimeException("ArrayConstraint is not select or store");
        }

        switch (c_compRef) {
        case EQ:

            if (selex != null && sel_right != null) {
                // The array constraint is a select
                ArrayExpression ae = selex.arrayExpression;
                pb.post(pb.eq(
                        pb.realSelect(pb.makeRealArrayVar(ae.getName()),
                                (selex.indexExpression instanceof IntegerConstant)
                                        ? pb.makeIntConst(((IntegerConstant) selex.indexExpression).value)
                                        : getExpression(selex.indexExpression)),
                        (sel_right instanceof RealConstant) ? pb.makeRealConst(((RealConstant) sel_right).value)
                                : getExpression(sel_right)));
                break;
            }
            if (stoex != null && sto_right != null) {
                // The array constraint is a store
                ArrayExpression ae = stoex.arrayExpression;
                ArrayExpression newae = sto_right;
                pb.post(pb.eq(pb.realStore(
                        pb.makeRealArrayVar(ae.getName()),
                        (stoex.indexExpression instanceof IntegerConstant)
                                ? pb.makeIntConst(((IntegerConstant) stoex.indexExpression).value)
                                : getExpression(stoex.indexExpression),
                        (stoex.value instanceof RealConstant) ? pb.makeRealConst(((RealConstant) stoex.value).value)
                                : getExpression(stoex.value)),
                        pb.makeRealArrayVar(newae.getName())));
                break;
            }
            throw new RuntimeException("ArrayConstraint is not correct select or store");
        case NE:
            if (selex != null && sel_right != null) {
                // The array constraint is a select
                ArrayExpression ae = selex.arrayExpression;
                pb.post(pb.neq(pb.realSelect(pb.makeRealArrayVar(ae.getName()), getExpression(selex.indexExpression)),
                        getExpression(sel_right)));
                break;
            }
            if (stoex != null && sto_right != null) {
                // The array constraint is a store
                ArrayExpression ae = stoex.arrayExpression;
                ArrayExpression newae = sto_right;
                pb.post(pb.neq(pb.realStore(pb.makeRealArrayVar(ae.getName()), getExpression(stoex.indexExpression),
                        getExpression(stoex.value)), newae));
                break;
            }
            throw new RuntimeException("ArrayConstraint is not correct select or store");
        default:
            throw new RuntimeException("ArrayConstraint is not select or store");
        }
        return true;
    }

}
