package com.altran.general.integration.xtextsirius.runtime.converter;

import java.util.Map;
import java.util.function.Function;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.properties.ControlDescription;
import org.eclipse.sirius.viewpoint.description.tool.InitialOperation;

import com.altran.general.integration.xtextsirius.model.eef.eefxtext.EefXtextSingleLineValueDescription;
import com.altran.general.integration.xtextsirius.model.eef.eefxtext.EefxtextFactory;
import com.altran.general.integration.xtextsirius.model.properties.propertiesxtext.XtextSingleLineValueDescription;

public class XtextSingleLineValueCompatibleDescriptionConverter implements ICompatibleDescriptionConverter {
	
	@Override
	public boolean canHandle(final EObject description) {
		return description instanceof XtextSingleLineValueDescription;
	}
	
	@Override
	public EObject convert(
			final EObject description,
			final Map<String, Object> parameters,
			final ICompatibleCache cache,
			final Function<ControlDescription, String> identifierGetter,
			final Function<InitialOperation, String> expressionForOperationGetter) {
		if (description instanceof XtextSingleLineValueDescription) {
			final XtextSingleLineValueDescription propertyDescription = (XtextSingleLineValueDescription) description;
			
			final EefXtextSingleLineValueDescription eefDescription = EefxtextFactory.eINSTANCE
					.createEefXtextSingleLineValueDescription();
			eefDescription.setIdentifier(identifierGetter.apply(propertyDescription));
			eefDescription.setHelpExpression(propertyDescription.getHelpExpression());
			eefDescription.setIsEnabledExpression(propertyDescription.getIsEnabledExpression());
			eefDescription.setLabelExpression(propertyDescription.getLabelExpression());
			
			eefDescription.setValueExpression(propertyDescription.getValueExpression());
			
			final InitialOperation initialOperation = propertyDescription.getInitialOperation();
			eefDescription.setEditExpression(expressionForOperationGetter.apply(initialOperation));
			
			eefDescription.setLineCount(1);
			
			eefDescription.setInjectorId(propertyDescription.getInjectorId());
			eefDescription.setMultiLine(propertyDescription.isMultiLine());
			
			eefDescription.setPrefixTextExpression(propertyDescription.getPrefixTextExpression());
			eefDescription.setSuffixTextExpression(propertyDescription.getSuffixTextExpression());
			
			// Let's not forget to populate the cache for the other converters
			// or link resolvers
			cache.put(propertyDescription, eefDescription);
			
			return eefDescription;
		}
		return null;
	}
	
}
