// Autogenerated, do not edit!
/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.dogma;
import com.opengamma.maths.commonapi.numbers.ComplexType;
import com.opengamma.maths.dogma.engine.language.InfixOperator;
import com.opengamma.maths.dogma.engine.language.UnaryFunction;
import com.opengamma.maths.dogma.engine.language.Function;
import com.opengamma.maths.dogma.engine.operationstack.InfixOpChain;
import com.opengamma.maths.dogma.engine.operationstack.MethodScraperForInfixOperators;
import com.opengamma.maths.dogma.engine.operationstack.MethodScraperForUnaryFunctions;
import com.opengamma.maths.dogma.engine.operationstack.OperatorDictionaryPopulator;
import com.opengamma.maths.dogma.engine.operationstack.RunInfixOpChain;
import com.opengamma.maths.dogma.engine.operationstack.RunUnaryFunctionChain;
import com.opengamma.maths.dogma.engine.operationstack.UnaryFunctionChain;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGArray;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGComplexScalar;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGMatrix;
import com.opengamma.maths.lowlevelapi.functions.checkers.Catchers;
import com.opengamma.maths.dogma.engine.matrixinfo.ConversionCostAdjacencyMatrixStore;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGRealScalar;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGMatrix;
import com.opengamma.maths.dogma.engine.matrixinfo.MatrixTypeToIndexMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.opengamma.maths.dogma.engine.methodhookinstances.binary.Horzcat;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Full;
import com.opengamma.maths.dogma.engine.methodhookinstances.infix.Mtimes;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Transpose;
import com.opengamma.maths.dogma.engine.methodhookinstances.infix.Plus;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Sin;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Ctranspose;
import com.opengamma.maths.dogma.engine.methodhookinstances.arbitrary.Vander;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Sparse;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Erf;
import com.opengamma.maths.dogma.engine.methodhookinstances.infix.Minus;
import com.opengamma.maths.dogma.engine.methodhookinstances.arbitrary.InvHilb;
import com.opengamma.maths.dogma.engine.methodhookinstances.infix.Rdivide;
import com.opengamma.maths.dogma.engine.methodhookinstances.arbitrary.Hilb;
import com.opengamma.maths.dogma.engine.methodhookinstances.arbitrary.Wilkinson;
import com.opengamma.maths.dogma.engine.methodhookinstances.binary.Dot;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Sqrt;
import com.opengamma.maths.dogma.engine.methodhookinstances.infix.Mldivide;
import com.opengamma.maths.dogma.engine.methodhookinstances.infix.Times;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Erfc;
import com.opengamma.maths.dogma.engine.methodhookinstances.unary.Copy;
import com.opengamma.maths.dogma.engine.methodhookinstances.arbitrary.Rosser;
/**
 * Provides the DOGMA Language
 */
public class DogmaLanguage {
private static DogmaLanguage s_instance;
DogmaLanguage() {
}
public static DogmaLanguage getInstance() {
return s_instance;
}
private static Logger s_log = LoggerFactory.getLogger(DogmaLanguage.class);// switch for chatty start up
private static boolean s_verbose;
public DogmaLanguage(boolean verbose) {
s_verbose = verbose;
};
private static RunInfixOpChain s_infixOpChainRunner = new RunInfixOpChain();
private static RunUnaryFunctionChain s_unaryFunctionChainRunner = new RunUnaryFunctionChain();
private static InfixOpChain[][] s_horzcatInstructions; //CSOFF
private static UnaryFunctionChain[] s_fullInstructions; //CSOFF
private static InfixOpChain[][] s_mtimesInstructions; //CSOFF
private static UnaryFunctionChain[] s_transposeInstructions; //CSOFF
private static InfixOpChain[][] s_plusInstructions; //CSOFF
private static UnaryFunctionChain[] s_sinInstructions; //CSOFF
private static UnaryFunctionChain[] s_ctransposeInstructions; //CSOFF
private static com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMASpecialMatrices.VanderFunction s_vanderfunction = new com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMASpecialMatrices.VanderFunction();
private static UnaryFunctionChain[] s_sparseInstructions; //CSOFF
private static UnaryFunctionChain[] s_erfInstructions; //CSOFF
private static InfixOpChain[][] s_minusInstructions; //CSOFF
private static com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMASpecialMatrices.InvhilbFunction s_invhilbfunction = new com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMASpecialMatrices.InvhilbFunction();
private static InfixOpChain[][] s_rdivideInstructions; //CSOFF
private static com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMASpecialMatrices.HilbFunction s_hilbfunction = new com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMASpecialMatrices.HilbFunction();
private static com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMASpecialMatrices.WilkinsonFunction s_wilkinsonfunction = new com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMASpecialMatrices.WilkinsonFunction();
private static InfixOpChain[][] s_dotInstructions; //CSOFF
private static UnaryFunctionChain[] s_sqrtInstructions; //CSOFF
private static InfixOpChain[][] s_mldivideInstructions; //CSOFF
private static InfixOpChain[][] s_timesInstructions; //CSOFF
private static UnaryFunctionChain[] s_erfcInstructions; //CSOFF
private static UnaryFunctionChain[] s_copyInstructions; //CSOFF
private static com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMASpecialMatrices.RosserFunction s_rosserfunction = new com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMASpecialMatrices.RosserFunction();
static {
if(s_verbose){
  s_log.info("Welcome to DOGMA");  s_log.info("Building instructions...");}
final double[][] DefaultInfixFunctionEvalCosts = new double[][] {
{1.00, 1.00, 1.00, 1.00, 0.00, 1.00, 1.00, 1.00, 1.00, 1.00 },//
{1.00, 1.00, 0.00, 1.00, 0.00, 0.00, 0.00, 1.00, 0.00, 1.00 },//
{1.00, 0.00, 1.00, 1.00, 0.00, 0.00, 1.00, 1.00, 1.00, 1.00 },//
{1.00, 1.00, 1.00, 1.00, 0.00, 0.00, 0.00, 1.00, 0.00, 1.00 },//
{0.00, 0.00, 0.00, 0.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00 },//
{1.00, 0.00, 0.00, 0.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00 },//
{1.00, 0.00, 1.00, 0.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00 },//
{1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 0.00, 1.00 },//
{1.00, 0.00, 1.00, 0.00, 1.00, 1.00, 1.00, 0.00, 1.00, 1.00 },//
{1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00 } };
OGMatrix defaultInfixFunctionEvalCostsMatrix = new OGMatrix(DefaultInfixFunctionEvalCosts);
final double[][] DefaultUnaryFunctionEvalCosts = new double[][] {//
{1 },//
{1 },//
{2 },//
{3 },//
{3 },//
{5 },//
{5 },//
{5 },//
{10 },//
{20 } };
OGMatrix defaultUnaryFunctionEvalCostsMatrix = new OGMatrix(DefaultUnaryFunctionEvalCosts);
// Build instructions sets
 OperatorDictionaryPopulator<InfixOperator<OGArray<? extends Number>, OGArray<? extends Number>, OGArray<? extends Number>>> operatorDictInfix = new OperatorDictionaryPopulator<InfixOperator<OGArray<? extends Number>, OGArray<? extends Number>, OGArray<? extends Number>>>();
OperatorDictionaryPopulator<UnaryFunction<OGArray<? extends Number>, OGArray<? extends Number>>> operatorDictUnary = new OperatorDictionaryPopulator<UnaryFunction<OGArray<? extends Number>, OGArray<? extends Number>>>();
InfixOperator<OGArray<? extends Number>, OGArray<? extends Number>, OGArray<? extends Number>>[][] HorzcatFunctionTable = MethodScraperForInfixOperators.availableMethodsForInfixOp(operatorDictInfix.getOperationsMap(),Horzcat.class, s_verbose);
s_horzcatInstructions = MethodScraperForInfixOperators.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),HorzcatFunctionTable, defaultInfixFunctionEvalCostsMatrix);

UnaryFunction<OGArray<? extends Number>, OGArray<? extends Number>>[] FullFunctionTable = MethodScraperForUnaryFunctions.availableMethodsForUnaryFunctions(operatorDictUnary.getOperationsMap(),Full.class);
s_fullInstructions = MethodScraperForUnaryFunctions.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),FullFunctionTable, defaultUnaryFunctionEvalCostsMatrix);

InfixOperator<OGArray<? extends Number>, OGArray<? extends Number>, OGArray<? extends Number>>[][] MtimesFunctionTable = MethodScraperForInfixOperators.availableMethodsForInfixOp(operatorDictInfix.getOperationsMap(),Mtimes.class, s_verbose);
s_mtimesInstructions = MethodScraperForInfixOperators.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),MtimesFunctionTable, defaultInfixFunctionEvalCostsMatrix);

UnaryFunction<OGArray<? extends Number>, OGArray<? extends Number>>[] TransposeFunctionTable = MethodScraperForUnaryFunctions.availableMethodsForUnaryFunctions(operatorDictUnary.getOperationsMap(),Transpose.class);
s_transposeInstructions = MethodScraperForUnaryFunctions.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),TransposeFunctionTable, defaultUnaryFunctionEvalCostsMatrix);

InfixOperator<OGArray<? extends Number>, OGArray<? extends Number>, OGArray<? extends Number>>[][] PlusFunctionTable = MethodScraperForInfixOperators.availableMethodsForInfixOp(operatorDictInfix.getOperationsMap(),Plus.class, s_verbose);
s_plusInstructions = MethodScraperForInfixOperators.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),PlusFunctionTable, defaultInfixFunctionEvalCostsMatrix);

UnaryFunction<OGArray<? extends Number>, OGArray<? extends Number>>[] SinFunctionTable = MethodScraperForUnaryFunctions.availableMethodsForUnaryFunctions(operatorDictUnary.getOperationsMap(),Sin.class);
s_sinInstructions = MethodScraperForUnaryFunctions.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),SinFunctionTable, defaultUnaryFunctionEvalCostsMatrix);

UnaryFunction<OGArray<? extends Number>, OGArray<? extends Number>>[] CtransposeFunctionTable = MethodScraperForUnaryFunctions.availableMethodsForUnaryFunctions(operatorDictUnary.getOperationsMap(),Ctranspose.class);
s_ctransposeInstructions = MethodScraperForUnaryFunctions.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),CtransposeFunctionTable, defaultUnaryFunctionEvalCostsMatrix);

UnaryFunction<OGArray<? extends Number>, OGArray<? extends Number>>[] SparseFunctionTable = MethodScraperForUnaryFunctions.availableMethodsForUnaryFunctions(operatorDictUnary.getOperationsMap(),Sparse.class);
s_sparseInstructions = MethodScraperForUnaryFunctions.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),SparseFunctionTable, defaultUnaryFunctionEvalCostsMatrix);

UnaryFunction<OGArray<? extends Number>, OGArray<? extends Number>>[] ErfFunctionTable = MethodScraperForUnaryFunctions.availableMethodsForUnaryFunctions(operatorDictUnary.getOperationsMap(),Erf.class);
s_erfInstructions = MethodScraperForUnaryFunctions.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),ErfFunctionTable, defaultUnaryFunctionEvalCostsMatrix);

InfixOperator<OGArray<? extends Number>, OGArray<? extends Number>, OGArray<? extends Number>>[][] MinusFunctionTable = MethodScraperForInfixOperators.availableMethodsForInfixOp(operatorDictInfix.getOperationsMap(),Minus.class, s_verbose);
s_minusInstructions = MethodScraperForInfixOperators.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),MinusFunctionTable, defaultInfixFunctionEvalCostsMatrix);

InfixOperator<OGArray<? extends Number>, OGArray<? extends Number>, OGArray<? extends Number>>[][] RdivideFunctionTable = MethodScraperForInfixOperators.availableMethodsForInfixOp(operatorDictInfix.getOperationsMap(),Rdivide.class, s_verbose);
s_rdivideInstructions = MethodScraperForInfixOperators.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),RdivideFunctionTable, defaultInfixFunctionEvalCostsMatrix);

InfixOperator<OGArray<? extends Number>, OGArray<? extends Number>, OGArray<? extends Number>>[][] DotFunctionTable = MethodScraperForInfixOperators.availableMethodsForInfixOp(operatorDictInfix.getOperationsMap(),Dot.class, s_verbose);
s_dotInstructions = MethodScraperForInfixOperators.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),DotFunctionTable, defaultInfixFunctionEvalCostsMatrix);

UnaryFunction<OGArray<? extends Number>, OGArray<? extends Number>>[] SqrtFunctionTable = MethodScraperForUnaryFunctions.availableMethodsForUnaryFunctions(operatorDictUnary.getOperationsMap(),Sqrt.class);
s_sqrtInstructions = MethodScraperForUnaryFunctions.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),SqrtFunctionTable, defaultUnaryFunctionEvalCostsMatrix);

InfixOperator<OGArray<? extends Number>, OGArray<? extends Number>, OGArray<? extends Number>>[][] MldivideFunctionTable = MethodScraperForInfixOperators.availableMethodsForInfixOp(operatorDictInfix.getOperationsMap(),Mldivide.class, s_verbose);
s_mldivideInstructions = MethodScraperForInfixOperators.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),MldivideFunctionTable, defaultInfixFunctionEvalCostsMatrix);

InfixOperator<OGArray<? extends Number>, OGArray<? extends Number>, OGArray<? extends Number>>[][] TimesFunctionTable = MethodScraperForInfixOperators.availableMethodsForInfixOp(operatorDictInfix.getOperationsMap(),Times.class, s_verbose);
s_timesInstructions = MethodScraperForInfixOperators.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),TimesFunctionTable, defaultInfixFunctionEvalCostsMatrix);

UnaryFunction<OGArray<? extends Number>, OGArray<? extends Number>>[] ErfcFunctionTable = MethodScraperForUnaryFunctions.availableMethodsForUnaryFunctions(operatorDictUnary.getOperationsMap(),Erfc.class);
s_erfcInstructions = MethodScraperForUnaryFunctions.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),ErfcFunctionTable, defaultUnaryFunctionEvalCostsMatrix);

UnaryFunction<OGArray<? extends Number>, OGArray<? extends Number>>[] CopyFunctionTable = MethodScraperForUnaryFunctions.availableMethodsForUnaryFunctions(operatorDictUnary.getOperationsMap(),Copy.class);
s_copyInstructions = MethodScraperForUnaryFunctions.computeFunctions(ConversionCostAdjacencyMatrixStore.getWeightedAdjacencyMatrix(),CopyFunctionTable, defaultUnaryFunctionEvalCostsMatrix);

if(s_verbose){
  s_log.info("DOGMA built.");}

}

public static OGArray<? extends Number>horzcat(OGArray<? extends Number> arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
  int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
  int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_horzcatInstructions[type1][type2], arg1, arg2);
  return tmp;
}

public static OGArray<? extends Number> horzcat(Number arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_horzcatInstructions[type1][type2], arg1rewrite, arg2);
  return tmp;
}

public static OGArray<? extends Number>horzcat(OGArray<? extends Number> arg1, Number arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg2rewrite;
if (arg2.getClass() == ComplexType.class) {
arg2rewrite = new OGComplexScalar(arg2);
} else {
 arg2rewrite = new OGRealScalar(arg2.doubleValue());
 }
 int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
 int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2rewrite.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_horzcatInstructions[type1][type2], arg1, arg2rewrite);
  return tmp;
}


public static OGArray<? extends Number>full(OGArray<? extends Number> arg1) {
Catchers.catchNullFromArgList(arg1, 1);
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_fullInstructions[type1], arg1);
return tmp;
}

public static OGArray<? extends Number>full(Number arg1) {Catchers.catchNullFromArgList(arg1, 1);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_fullInstructions[type1], arg1rewrite);
return tmp;
}


public static OGArray<? extends Number>mtimes(OGArray<? extends Number> arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
  int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
  int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_mtimesInstructions[type1][type2], arg1, arg2);
  return tmp;
}

public static OGArray<? extends Number> mtimes(Number arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_mtimesInstructions[type1][type2], arg1rewrite, arg2);
  return tmp;
}

public static OGArray<? extends Number>mtimes(OGArray<? extends Number> arg1, Number arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg2rewrite;
if (arg2.getClass() == ComplexType.class) {
arg2rewrite = new OGComplexScalar(arg2);
} else {
 arg2rewrite = new OGRealScalar(arg2.doubleValue());
 }
 int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
 int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2rewrite.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_mtimesInstructions[type1][type2], arg1, arg2rewrite);
  return tmp;
}


public static OGArray<? extends Number>transpose(OGArray<? extends Number> arg1) {
Catchers.catchNullFromArgList(arg1, 1);
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_transposeInstructions[type1], arg1);
return tmp;
}

public static OGArray<? extends Number>transpose(Number arg1) {Catchers.catchNullFromArgList(arg1, 1);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_transposeInstructions[type1], arg1rewrite);
return tmp;
}


public static OGArray<? extends Number>plus(OGArray<? extends Number> arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
  int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
  int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_plusInstructions[type1][type2], arg1, arg2);
  return tmp;
}

public static OGArray<? extends Number> plus(Number arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_plusInstructions[type1][type2], arg1rewrite, arg2);
  return tmp;
}

public static OGArray<? extends Number>plus(OGArray<? extends Number> arg1, Number arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg2rewrite;
if (arg2.getClass() == ComplexType.class) {
arg2rewrite = new OGComplexScalar(arg2);
} else {
 arg2rewrite = new OGRealScalar(arg2.doubleValue());
 }
 int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
 int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2rewrite.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_plusInstructions[type1][type2], arg1, arg2rewrite);
  return tmp;
}


public static OGArray<? extends Number>sin(OGArray<? extends Number> arg1) {
Catchers.catchNullFromArgList(arg1, 1);
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_sinInstructions[type1], arg1);
return tmp;
}

public static OGArray<? extends Number>sin(Number arg1) {Catchers.catchNullFromArgList(arg1, 1);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_sinInstructions[type1], arg1rewrite);
return tmp;
}


public static OGArray<? extends Number>ctranspose(OGArray<? extends Number> arg1) {
Catchers.catchNullFromArgList(arg1, 1);
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_ctransposeInstructions[type1], arg1);
return tmp;
}

public static OGArray<? extends Number>ctranspose(Number arg1) {Catchers.catchNullFromArgList(arg1, 1);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_ctransposeInstructions[type1], arg1rewrite);
return tmp;
}

public static OGMatrix vander(OGMatrix arg0, int arg1 ){
// vander
return s_vanderfunction.vander( arg0,  arg1);
};
public static OGMatrix vander(OGMatrix arg0 ){
// vander
return s_vanderfunction.vander( arg0);
};

public static OGArray<? extends Number>sparse(OGArray<? extends Number> arg1) {
Catchers.catchNullFromArgList(arg1, 1);
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_sparseInstructions[type1], arg1);
return tmp;
}

public static OGArray<? extends Number>sparse(Number arg1) {Catchers.catchNullFromArgList(arg1, 1);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_sparseInstructions[type1], arg1rewrite);
return tmp;
}


public static OGArray<? extends Number>erf(OGArray<? extends Number> arg1) {
Catchers.catchNullFromArgList(arg1, 1);
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_erfInstructions[type1], arg1);
return tmp;
}

public static OGArray<? extends Number>erf(Number arg1) {Catchers.catchNullFromArgList(arg1, 1);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_erfInstructions[type1], arg1rewrite);
return tmp;
}


public static OGArray<? extends Number>minus(OGArray<? extends Number> arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
  int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
  int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_minusInstructions[type1][type2], arg1, arg2);
  return tmp;
}

public static OGArray<? extends Number> minus(Number arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_minusInstructions[type1][type2], arg1rewrite, arg2);
  return tmp;
}

public static OGArray<? extends Number>minus(OGArray<? extends Number> arg1, Number arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg2rewrite;
if (arg2.getClass() == ComplexType.class) {
arg2rewrite = new OGComplexScalar(arg2);
} else {
 arg2rewrite = new OGRealScalar(arg2.doubleValue());
 }
 int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
 int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2rewrite.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_minusInstructions[type1][type2], arg1, arg2rewrite);
  return tmp;
}

public static OGMatrix invhilb(int arg0 ){
// invhilb
return s_invhilbfunction.invhilb( arg0);
};

public static OGArray<? extends Number>rdivide(OGArray<? extends Number> arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
  int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
  int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_rdivideInstructions[type1][type2], arg1, arg2);
  return tmp;
}

public static OGArray<? extends Number> rdivide(Number arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_rdivideInstructions[type1][type2], arg1rewrite, arg2);
  return tmp;
}

public static OGArray<? extends Number>rdivide(OGArray<? extends Number> arg1, Number arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg2rewrite;
if (arg2.getClass() == ComplexType.class) {
arg2rewrite = new OGComplexScalar(arg2);
} else {
 arg2rewrite = new OGRealScalar(arg2.doubleValue());
 }
 int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
 int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2rewrite.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_rdivideInstructions[type1][type2], arg1, arg2rewrite);
  return tmp;
}

public static OGMatrix hilb(int arg0 ){
// hilb
return s_hilbfunction.hilb( arg0);
};
public static OGMatrix wilkinson(int arg0 ){
// wilkinson
return s_wilkinsonfunction.wilkinson( arg0);
};

public static OGArray<? extends Number>dot(OGArray<? extends Number> arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
  int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
  int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_dotInstructions[type1][type2], arg1, arg2);
  return tmp;
}

public static OGArray<? extends Number> dot(Number arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_dotInstructions[type1][type2], arg1rewrite, arg2);
  return tmp;
}

public static OGArray<? extends Number>dot(OGArray<? extends Number> arg1, Number arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg2rewrite;
if (arg2.getClass() == ComplexType.class) {
arg2rewrite = new OGComplexScalar(arg2);
} else {
 arg2rewrite = new OGRealScalar(arg2.doubleValue());
 }
 int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
 int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2rewrite.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_dotInstructions[type1][type2], arg1, arg2rewrite);
  return tmp;
}


public static OGArray<? extends Number>sqrt(OGArray<? extends Number> arg1) {
Catchers.catchNullFromArgList(arg1, 1);
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_sqrtInstructions[type1], arg1);
return tmp;
}

public static OGArray<? extends Number>sqrt(Number arg1) {Catchers.catchNullFromArgList(arg1, 1);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_sqrtInstructions[type1], arg1rewrite);
return tmp;
}


public static OGArray<? extends Number>mldivide(OGArray<? extends Number> arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
  int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
  int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_mldivideInstructions[type1][type2], arg1, arg2);
  return tmp;
}

public static OGArray<? extends Number> mldivide(Number arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_mldivideInstructions[type1][type2], arg1rewrite, arg2);
  return tmp;
}

public static OGArray<? extends Number>mldivide(OGArray<? extends Number> arg1, Number arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg2rewrite;
if (arg2.getClass() == ComplexType.class) {
arg2rewrite = new OGComplexScalar(arg2);
} else {
 arg2rewrite = new OGRealScalar(arg2.doubleValue());
 }
 int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
 int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2rewrite.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_mldivideInstructions[type1][type2], arg1, arg2rewrite);
  return tmp;
}


public static OGArray<? extends Number>times(OGArray<? extends Number> arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
  int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
  int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_timesInstructions[type1][type2], arg1, arg2);
  return tmp;
}

public static OGArray<? extends Number> times(Number arg1, OGArray<? extends Number> arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_timesInstructions[type1][type2], arg1rewrite, arg2);
  return tmp;
}

public static OGArray<? extends Number>times(OGArray<? extends Number> arg1, Number arg2) {
  Catchers.catchNullFromArgList(arg1, 1);
  Catchers.catchNullFromArgList(arg2, 2);
OGArray<? extends Number> arg2rewrite;
if (arg2.getClass() == ComplexType.class) {
arg2rewrite = new OGComplexScalar(arg2);
} else {
 arg2rewrite = new OGRealScalar(arg2.doubleValue());
 }
 int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
 int type2 = MatrixTypeToIndexMap.getIndexFromClass(arg2rewrite.getClass());
  OGArray<? extends Number> tmp = s_infixOpChainRunner.dispatch(s_timesInstructions[type1][type2], arg1, arg2rewrite);
  return tmp;
}


public static OGArray<? extends Number>erfc(OGArray<? extends Number> arg1) {
Catchers.catchNullFromArgList(arg1, 1);
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_erfcInstructions[type1], arg1);
return tmp;
}

public static OGArray<? extends Number>erfc(Number arg1) {Catchers.catchNullFromArgList(arg1, 1);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_erfcInstructions[type1], arg1rewrite);
return tmp;
}


public static OGArray<? extends Number>copy(OGArray<? extends Number> arg1) {
Catchers.catchNullFromArgList(arg1, 1);
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_copyInstructions[type1], arg1);
return tmp;
}

public static OGArray<? extends Number>copy(Number arg1) {Catchers.catchNullFromArgList(arg1, 1);
OGArray<? extends Number> arg1rewrite;
if (arg1.getClass() == ComplexType.class) {
arg1rewrite = new OGComplexScalar(arg1);
} else {
arg1rewrite = new OGRealScalar(arg1.doubleValue());
}
int type1 = MatrixTypeToIndexMap.getIndexFromClass(arg1rewrite.getClass());
OGArray<? extends Number> tmp = s_unaryFunctionChainRunner.dispatch(s_copyInstructions[type1], arg1rewrite);
return tmp;
}

public static OGMatrix rosser( ){
// rosser
return s_rosserfunction.rosser();
};

}
