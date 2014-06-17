/*
 * Copyright 2014 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sarl.lang.util;

import io.sarl.lang.signature.ActionKey;
import io.sarl.lang.signature.ActionSignatureProvider;
import io.sarl.lang.signature.SignatureKey;

import java.lang.reflect.Modifier;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmAnnotationTarget;
import org.eclipse.xtext.common.types.JvmAnnotationType;
import org.eclipse.xtext.common.types.JvmAnnotationValue;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmExecutable;
import org.eclipse.xtext.common.types.JvmFeature;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmStringAnnotationValue;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.typesystem.conformance.TypeConformanceComputationArgument;
import org.eclipse.xtext.xbase.typesystem.legacy.StandardTypeReferenceOwner;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.OwnedConverter;
import org.eclipse.xtext.xbase.typesystem.util.CommonTypeComputationServices;

import com.ibm.icu.util.VersionInfo;

/**
 * Utilities functions on JvmElements.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class ModelUtil {

	private static final String PREFIX_ACTION_HANDLE = "_handle_"; //$NON-NLS-1$
	private static final String PREFIX_ATTRIBUTE_DEFAULT_VALUE = "___FORMAL_PARAMETER_DEFAULT_VALUE_"; //$NON-NLS-1$

	/** Analyzing the type hierarchy of the given interface and
	 * extract hierarchy information.
	 * 
	 * @param jvmElement - the element to analyze
	 * @param operations - filled with the operations inside and inherited by the element.
	 * @param fields - filled with the fields inside and inherited by the element.
	 * @param sarlSignatureProvider - provider of tools related to action signatures.
	 */
	public static void populateInterfaceElements(
			JvmGenericType jvmElement,
			Map<ActionKey,JvmOperation> operations,
			Map<String, JvmField> fields,
			ActionSignatureProvider sarlSignatureProvider) {
		for(JvmFeature feature : jvmElement.getAllFeatures()) {
			if (!"java.lang.Object".equals(feature.getDeclaringType().getQualifiedName())) { //$NON-NLS-1$
				if (operations!=null && feature instanceof JvmOperation) {
					JvmOperation operation = (JvmOperation)feature;
					SignatureKey sig = sarlSignatureProvider.createSignatureIDFromJvmModel(operation.isVarArgs(), operation.getParameters());
					ActionKey actionKey = sarlSignatureProvider.createActionID(operation.getSimpleName(), sig);
					operations.put(actionKey, operation);
				}
				else if (fields!=null && feature instanceof JvmField) {
					fields.put(feature.getSimpleName(), (JvmField)feature);
				}
			}
		}
	}

	/** Analyzing the type hierarchy of the given element, and
	 * extract any type-related information.
	 * 
	 * @param jvmElement - the element to analyze
	 * @param finalOperations - filled with the final operations inherited by the element.
	 * @param overridableOperations - filled with the oervrideable operations inherited by the element.
	 * @param inheritedFields - filled with the fields inherited by the element.
	 * @param operationsToImplement - filled with the abstract operations inherited by the element.
	 * @param superConstructors - filled with the construstors of the super type.
	 * @param sarlSignatureProvider - provider of tools related to action signatures.
	 */
	public static void populateInheritanceContext(
			JvmGenericType jvmElement,
			Map<ActionKey,JvmOperation> finalOperations,
			Map<ActionKey,JvmOperation> overridableOperations,
			Map<String,JvmField> inheritedFields,
			Map<ActionKey,JvmOperation> operationsToImplement,
			Map<SignatureKey,JvmConstructor> superConstructors,
			ActionSignatureProvider sarlSignatureProvider) {

		// Get the operations that must be implemented
		if (operationsToImplement!=null) {
			for(JvmTypeReference interfaceReference : jvmElement.getExtendedInterfaces()) {
				for(JvmFeature feature : ((JvmGenericType)interfaceReference.getType()).getAllFeatures()) {
					if (!"java.lang.Object".equals(feature.getDeclaringType().getQualifiedName())) { //$NON-NLS-1$
						if (feature instanceof JvmOperation) {
							JvmOperation operation = (JvmOperation)feature;
							SignatureKey sig = sarlSignatureProvider.createSignatureIDFromJvmModel(operation.isVarArgs(), operation.getParameters());
							ActionKey actionKey = sarlSignatureProvider.createActionID(operation.getSimpleName(), sig);
							operationsToImplement.put(actionKey, operation);
						}
					}
				}
			}
		}

		// Check on the implemented features, inherited from the super type			
		if (jvmElement.getExtendedClass()!=null) {
			JvmGenericType parentType = (JvmGenericType)jvmElement.getExtendedClass().getType();
			for(JvmFeature feature : parentType.getAllFeatures()) {
				if (!"java.lang.Object".equals(feature.getDeclaringType().getQualifiedName()) //$NON-NLS-1$
						&& isVisible(jvmElement, feature)
						&& !isHiddenAction(feature.getSimpleName())) {
					if (feature instanceof JvmOperation) {
						if (!feature.isStatic()) {
							JvmOperation operation = (JvmOperation)feature;
							SignatureKey sig = sarlSignatureProvider.createSignatureIDFromJvmModel(operation.isVarArgs(), operation.getParameters());
							ActionKey actionKey = sarlSignatureProvider.createActionID(feature.getSimpleName(), sig);
							if (operation.isAbstract()) {
								if (operationsToImplement!=null) operationsToImplement.put(actionKey, operation);
							}
							else if (operation.isFinal()) {
								if (finalOperations!=null) finalOperations.put(actionKey, operation);
								if (operationsToImplement!=null) operationsToImplement.remove(actionKey);
							}
							else {
								if (overridableOperations!=null) overridableOperations.put(actionKey, operation);
								if (operationsToImplement!=null) operationsToImplement.remove(actionKey);
							}
						} 
					}
					else if (feature instanceof JvmField && inheritedFields!=null) {
						inheritedFields.put(feature.getSimpleName(), (JvmField)feature);
					}
				}
			}

			if (superConstructors!=null) {
				for(JvmConstructor cons : parentType.getDeclaredConstructors()) {
					SignatureKey sig = sarlSignatureProvider.createSignatureIDFromJvmModel(cons.isVarArgs(), cons.getParameters());
					superConstructors.put(sig,  cons);
				}
			}
		}
	}

	/** Replies if the target feature is visible from the type.
	 * 
	 * @param fromType
	 * @param target
	 * @return <code>true</code> if the given type can see the target feature.
	 */
	public static boolean isVisible(JvmDeclaredType fromType, JvmMember target) {
		switch(target.getVisibility()) {
		case DEFAULT: {
			return target.getDeclaringType().getPackageName().equals(fromType.getPackageName());
		}
		case PROTECTED:
		case PUBLIC:
			return true;
		case PRIVATE:
		default:
		}
		return false;
	}

	/** Replies if the given name is related to an hidden action.
	 * <p>
	 * An hidden action is an action that is generated by the SARL
	 * compiler, and that cannot be defined by the SARL user.
	 * 
	 * @param name
	 * @return <code>true</code> if the given name is reserved by
	 * SARL.
	 */
	public static boolean isHiddenAction(String name) {
		return name.startsWith(PREFIX_ACTION_HANDLE);
	}

	/** Replies a fixed version of the given name assuming
	 * that it is an hidden action.
	 * <p>
	 * An hidden action is an action that is generated by the SARL
	 * compiler, and that cannot be defined by the SARL user.
	 * 
	 * @param name
	 * @return the fixed name.
	 */
	public static String fixHiddenAction(String name) {
		if (name.startsWith(PREFIX_ACTION_HANDLE)) {
			if (name.length()>PREFIX_ACTION_HANDLE.length()) {
				return "handle"+name.substring(PREFIX_ACTION_HANDLE.length(),PREFIX_ACTION_HANDLE.length()+1).toUpperCase()+name.substring(PREFIX_ACTION_HANDLE.length()+1); //$NON-NLS-1$
			}
			return "handle"; //$NON-NLS-1$
		}
		return name;
	}

	/** Replies if the given name is related to an hidden attribute.
	 * <p>
	 * An hidden attribute is an attribute that is generated by the SARL
	 * compiler, and that cannot be defined by the SARL user.
	 * 
	 * @param name
	 * @return <code>true</code> if the given name is reserved by
	 * SARL.
	 */
	public static boolean isHiddenAttribute(String name) {
		return name.startsWith(PREFIX_ATTRIBUTE_DEFAULT_VALUE);
	}

	/** Replies a fixed version of the given name assuming
	 * that it is an hidden attribute.
	 * <p>
	 * An hidden attribute is an attribute that is generated by the SARL
	 * compiler, and that cannot be defined by the SARL user.
	 * 
	 * @param name
	 * @return the fixed name.
	 */
	public static String fixHiddenAttribute(String name) {
		if (name.startsWith(PREFIX_ATTRIBUTE_DEFAULT_VALUE)) {
			return name.substring(PREFIX_ATTRIBUTE_DEFAULT_VALUE.length(),PREFIX_ATTRIBUTE_DEFAULT_VALUE.length()+1).toLowerCase()+name.substring(PREFIX_ATTRIBUTE_DEFAULT_VALUE.length()+1);
		}
		return name;
	}

	/** Replies if the given reference is pointing to a class type.
	 * 
	 * @param typeRef
	 * @return <code>true</code> if the pointed element is a class type.
	 */
	public static boolean isClass(LightweightTypeReference typeRef) {
		JvmType t = typeRef.getType();
		if (t instanceof JvmGenericType) {
			return !((JvmGenericType)t).isInterface();
		}
		return false;
	}	

	/** Replies if the given type is a class type.
	 * 
	 * @param type
	 * @return <code>true</code> if the element is a class type.
	 */
	public static boolean isClass(Class<?> type) {
		return !type.isInterface();
	}	

	/** Replies if the given reference is referencing a final type.
	 * 
	 * @param expressionTypeRef
	 * @return <code>true</code> if the given type is final.
	 */
	public static boolean isFinal(LightweightTypeReference expressionTypeRef) {
		if (expressionTypeRef.isArray()) {
			return isFinal(expressionTypeRef.getComponentType());
		}
		if (expressionTypeRef.isPrimitive())
			return true;
		return expressionTypeRef.getType() instanceof JvmDeclaredType
				&& ((JvmDeclaredType) expressionTypeRef.getType()).isFinal();
	}

	/** Replies if the given type is a final type.
	 * 
	 * @param expressionType
	 * @return <code>true</code> if the given type is final.
	 */
	public static boolean isFinal(Class<?> expressionType) {
		if (expressionType.isArray()) {
			return isFinal(expressionType.getComponentType());
		}
		if (expressionType.isPrimitive())
			return true;
		return expressionType.isEnum() || 
				Modifier.isFinal(expressionType.getModifiers());
	}

	/** Replies if the given type is an interface.
	 * 
	 * @param type
	 * @return <code>true</code> if the given type is an interface.
	 */
	public static boolean isInterface(LightweightTypeReference type) {
		return type.getType() instanceof JvmGenericType && ((JvmGenericType)type.getType()).isInterface();
	}

	/** Replies if it is allowed to cast between the given types.
	 * 
	 * @param fromType - source type
	 * @param toType - target type
	 * @param enablePrimitiveWidening - indicates if the widening of the primitive types is allowed.
	 * @param enableVoidMatchingNull - indicates if the <code>null</code> is matching <code>void</code>.
	 * @param allowSynonyms - indicates if the synonyms are allowed.
	 * @return the state of the cast.
	 */
	public static boolean canCast(LightweightTypeReference fromType, LightweightTypeReference toType, boolean enablePrimitiveWidening, boolean enableVoidMatchingNull, boolean allowSynonyms) {
		if (enableVoidMatchingNull) {
			boolean fromVoid = (fromType==null) || (fromType.isPrimitiveVoid());
			boolean toVoid = (toType==null) || (toType.isPrimitiveVoid());
			if (fromVoid) return toVoid;
			if (toVoid) return fromVoid;
			assert(fromType!=null);
			assert(toType!=null);
		}
		else {
			if (fromType==null || toType==null) return false;
			if (fromType.isPrimitiveVoid()!=toType.isPrimitiveVoid()) {
				return false;
			}
		}
//		if (fromType.isUnknown() || toType.isUnknown()) return false;
		TypeConformanceComputationArgument conform = new TypeConformanceComputationArgument(
				false, false, true, enablePrimitiveWidening, false, allowSynonyms);
		if (fromType.getType() instanceof JvmDeclaredType || fromType.isPrimitive()) {
			// if one of the types is an interface and the other is a non final class (or interface) there always can be a subtype
			if ((!isInterface(fromType) || isFinal(toType)) && (!isInterface(toType) || isFinal(fromType))) { 
				if (!toType.isAssignableFrom(fromType, conform)) {
					if (   isFinal(fromType) || isFinal(toType)
							|| isClass(fromType) && isClass(toType)) {
						if (!fromType.isAssignableFrom(toType, conform)) { // no upcast
							return false;
						}
					}
				}
			}
		}
		if(toType.isPrimitive() && !(fromType.isPrimitive() || fromType.isWrapper())) {
			return false;
		}
		return true;
	}

	/** Convert a type reference to a lightweight type reference.
	 * 
	 * @param typeRef - reference to convert.
	 * @param services - services used for the conversion
	 * @return the lightweight type reference.
	 */
	public static LightweightTypeReference toLightweightTypeReference(JvmTypeReference typeRef, CommonTypeComputationServices services) {
		return toLightweightTypeReference(typeRef, services, false);
	}

	/** Convert a type reference to a lightweight type reference.
	 * 
	 * @param typeRef - reference to convert.
	 * @param services - services used for the conversion
	 * @param keepUnboundWildcardInformation - indicates if the unbound wild card information must be keeped in the lightweight reference.
	 * @return the lightweight type reference.
	 */
	public static LightweightTypeReference toLightweightTypeReference(JvmTypeReference typeRef, CommonTypeComputationServices services, boolean keepUnboundWildcardInformation) {
		if (typeRef==null) return null;
		OwnedConverter converter = new OwnedConverter(new StandardTypeReferenceOwner(services, typeRef), keepUnboundWildcardInformation);
		LightweightTypeReference reference = converter.toLightweightReference(typeRef);
		return reference;
	}

	/** Extract the string value of the given annotation, if it exists.
	 * 
	 * @param op - the annoted element.
	 * @param annotationType - the type of the annotation to consider
	 * @return the value of the annotation, or <code>null</code> if no annotation or no
	 * value.
	 */
	public static String annotationString(JvmAnnotationTarget op, Class<?> annotationType) {
		String n = annotationType.getName();
		for(JvmAnnotationReference aref : op.getAnnotations()) {
			JvmAnnotationType an = aref.getAnnotation();
			if (n!=null && n.equals(an.getQualifiedName())) {
				for(JvmAnnotationValue value : aref.getValues()) {
					if (value instanceof JvmStringAnnotationValue) {
						for(String sValue : ((JvmStringAnnotationValue)value).getValues()) {
							if (sValue!=null) return sValue;
						}
					}
				}
			}
		}
		return null;
	}
	
	/** Compare the two strings as they are version numbers.
	 * 
	 * @param v1
	 * @param v2
	 * @return Negative integer of <var>v1</var> is lower than <var>v2</var>;
	 * positive integer of <var>v1</var> is greater than <var>v2</var>;
	 * {@code 0} if they are strictly equal.
	 */
	public static int compareVersions(String v1, String v2) {
		VersionInfo vi1 = VersionInfo.getInstance(v1);
		VersionInfo vi2 = VersionInfo.getInstance(v2);
		return vi1.compareTo(vi2);
	}
	
	/** Create a string representation of the prototype of the given executable.
	 * 
	 * @param e
	 * @return the prototype
	 */
	public static String toActionProtoptypeString(JvmExecutable e) {
		StringBuilder b = new StringBuilder();
		JvmTypeReference returnType;
		if (e instanceof JvmConstructor) {
			b.append("new"); //$NON-NLS-1$
			returnType = null;
		}
		else {
			JvmOperation op = (JvmOperation)e;
			b.append("def "); //$NON-NLS-1$
			b.append(op.getSimpleName());
			returnType = op.getReturnType();
		}
		EList<JvmFormalParameter> params = e.getParameters();
		if (!params.isEmpty()) {
			b.append("("); //$NON-NLS-1$
			boolean first = true;
			for(JvmFormalParameter param : params) {
				if (first) first = false;
				else b.append(", "); //$NON-NLS-1$
				b.append(param.getName());
				b.append(" : "); //$NON-NLS-1$
				b.append(param.getParameterType().getIdentifier());
			}
			b.append(")"); //$NON-NLS-1$
		}
		if (returnType!=null && !"void".equals(returnType.getIdentifier())) { //$NON-NLS-1$
			b.append(" : "); //$NON-NLS-1$
			b.append(returnType.getIdentifier());
		}
		return b.toString();
	}

	/** Replies the default value for the given type.
	 * 
	 * @param type
	 * @return the default value.
	 */
	public static String getDefaultValueForType(LightweightTypeReference type) {
		//TODO: Check if a similar function exists in the Xbase library.
		String defaultValue = ""; //$NON-NLS-1$
		if (type!=null && !type.isPrimitiveVoid()) {
			switch(type.getIdentifier()) {
			case "boolean":  //$NON-NLS-1$
				defaultValue = "true"; //$NON-NLS-1$
				break;
			case "double":  //$NON-NLS-1$
				defaultValue = "0.0"; //$NON-NLS-1$
				break;
			case "float":  //$NON-NLS-1$
				defaultValue = "0.0f"; //$NON-NLS-1$
				break;
			case "int":  //$NON-NLS-1$
				defaultValue = "0"; //$NON-NLS-1$
				break;
			case "long":  //$NON-NLS-1$
				defaultValue = "0"; //$NON-NLS-1$
				break;
			case "byte": //$NON-NLS-1$
				defaultValue = "(0 as byte)"; //$NON-NLS-1$
				break;
			case "short":  //$NON-NLS-1$
				defaultValue = "(0 as short)"; //$NON-NLS-1$
				break;
			case "char":  //$NON-NLS-1$
				defaultValue = "(0 as char)"; //$NON-NLS-1$
				break;
			default: 
				defaultValue = "null"; //$NON-NLS-1$
			}
		}
		return defaultValue;
	}
	
}
