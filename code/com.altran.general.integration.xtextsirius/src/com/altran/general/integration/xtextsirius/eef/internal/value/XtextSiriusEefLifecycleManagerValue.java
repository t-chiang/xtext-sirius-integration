package com.altran.general.integration.xtextsirius.eef.internal.value;

import org.eclipse.eef.EEFTextDescription;
import org.eclipse.eef.common.ui.api.IEEFFormContainer;
import org.eclipse.eef.core.api.EEFExpressionUtils;
import org.eclipse.eef.core.api.EditingContextAdapter;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.sirius.common.interpreter.api.IInterpreter;
import org.eclipse.sirius.common.interpreter.api.IVariableManager;
import org.eclipse.swt.widgets.Composite;

import com.altran.general.integration.xtextsirius.eef.internal.AXtextSiriusEefLifecycleManager;
import com.altran.general.integration.xtextsirius.eef.internal.XtextSiriusController;
import com.google.inject.Injector;

public class XtextSiriusEefLifecycleManagerValue extends AXtextSiriusEefLifecycleManager {
	
	public XtextSiriusEefLifecycleManagerValue(
			final @NonNull PropertyDescriptorValue descriptor,
			final @NonNull EEFTextDescription controlDescription,
			final @NonNull IVariableManager variableManager,
			final @NonNull IInterpreter interpreter,
			final @NonNull EditingContextAdapter contextAdapter) {
		super(descriptor, controlDescription, variableManager, interpreter, contextAdapter);
	}
	
	@Override
	protected void createMainControl(final Composite parent, final IEEFFormContainer formContainer) {
		final Injector injector = createSpecializedInjector();

		this.widget = new XtextSiriusWidgetValue(parent, injector, getDescriptor().isMultiLine(),
				getDescriptor().getPrefixText(),
				getDescriptor().getSuffixText());
		applyGridData(getWidget().getControl());

		this.controller = new XtextSiriusController(this.controlDescription, this.variableManager, this.interpreter,
				this.contextAdapter);
	}
	
	@Override
	public XtextSiriusWidgetValue getWidget() {
		return (XtextSiriusWidgetValue) super.getWidget();
	}
	
	@Override
	public @NonNull PropertyDescriptorValue getDescriptor() {
		return (@NonNull PropertyDescriptorValue) super.getDescriptor();
	}

	@Override
	public void aboutToBeShown() {
		super.aboutToBeShown();

		this.newValueConsumer = (newValue) -> {
			if (newValue instanceof String) {
				getWidget().update((String) newValue);
				final Object self = this.variableManager.getVariables().get(EEFExpressionUtils.SELF);
				if (self instanceof EObject) {
					getWidget().updateUri(((EObject) self).eResource().getURI());
				}
			}
		};
		this.controller.onNewValue(this.newValueConsumer);
	}

	@Override
	public void aboutToBeHidden() {
		persistIfDirty(getWidget().getText());
		super.aboutToBeHidden();
	}
}
