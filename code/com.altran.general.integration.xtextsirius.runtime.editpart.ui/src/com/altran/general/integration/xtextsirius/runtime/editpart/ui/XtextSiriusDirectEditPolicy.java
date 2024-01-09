/**
 * Copyright (C) 2018 Altran Netherlands B.V.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package com.altran.general.integration.xtextsirius.runtime.editpart.ui;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.LabelDirectEditPolicy;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.sirius.business.api.helper.SiriusUtil;
import org.eclipse.sirius.business.api.helper.task.ICommandTask;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.internal.helper.task.ModelOperationToTask;
import org.eclipse.sirius.business.internal.helper.task.operations.AbstractOperationTask;
import org.eclipse.sirius.business.internal.helper.task.operations.ForTask;
import org.eclipse.sirius.diagram.business.api.query.EObjectQuery;
import org.eclipse.sirius.diagram.description.DiagramElementMapping;
import org.eclipse.sirius.diagram.description.tool.DirectEditLabel;
import org.eclipse.sirius.diagram.ui.tools.api.command.GMFCommandWrapper;
import org.eclipse.sirius.diagram.ui.tools.internal.commands.emf.EMFCommandFactoryUI;
import org.eclipse.sirius.ecore.extender.business.api.accessor.ModelAccessor;
import org.eclipse.sirius.tools.api.SiriusPlugin;
import org.eclipse.sirius.tools.api.command.CommandContext;
import org.eclipse.sirius.tools.api.command.SiriusCommand;
import org.eclipse.sirius.viewpoint.DRepresentationElement;
import org.eclipse.sirius.viewpoint.description.RepresentationElementMapping;
import org.eclipse.sirius.viewpoint.description.tool.ContainerModelOperation;
import org.eclipse.sirius.viewpoint.description.tool.InitialOperation;
import org.eclipse.sirius.viewpoint.description.tool.ModelOperation;
import org.eclipse.sirius.viewpoint.description.tool.SetValue;
import org.eclipse.ui.statushandlers.StatusManager;
import org.yakindu.base.xtext.utils.gmf.directedit.IXtextAwareEditPart;

import com.altran.general.integration.xtextsirius.runtime.exception.AXtextSiriusIssueException;
import com.altran.general.integration.xtextsirius.runtime.task.ReplaceValueTask;

@SuppressWarnings("restriction")
public class XtextSiriusDirectEditPolicy extends LabelDirectEditPolicy {
	@Override
	protected void showCurrentEditValue(final DirectEditRequest request) {
		final String value = (String) request.getCellEditor().getValue();
		((IXtextAwareEditPart) getHost()).setLabelText(value);
	}

	@Override
	protected Command getDirectEditCommand(final DirectEditRequest edit) {
		final CellEditor cellEditor = edit.getCellEditor();

		if (!cellEditor.isDirty()) {
			return null;
		}

		if (!(cellEditor instanceof AXtextSiriusStyledTextCellEditor)) {
			return null;
		}

		final DRepresentationElement representationElement = extractRepresentationElement();
		if (representationElement == null) {
			return null;
		}

		try {
			final AXtextSiriusStyledTextCellEditor xtextSiriusCellEditor = (AXtextSiriusStyledTextCellEditor) cellEditor;
			final TransactionalEditingDomain editingDomain = TransactionUtil
					.getEditingDomain(representationElement.getTarget());

			final SiriusCommand siriusCommand = new SiriusCommand(editingDomain);
			final ReplaceValueTask task = new ReplaceValueTask(representationElement,
					representationTarget -> xtextSiriusCellEditor.commit(representationTarget));
			addChildTasks(xtextSiriusCellEditor, representationElement, editingDomain, task);
			
			siriusCommand.getTasks().add(task);

			return new ICommandProxy(new GMFCommandWrapper(editingDomain, siriusCommand));
		} catch (final AXtextSiriusIssueException ex) {
			StatusManager.getManager().handle(ex.toStatus(), StatusManager.SHOW);
			return null;
		}
	}

	/**
	 * This is a pretty hacky way to simulate the same behavior as
	 * {@link org.eclipse.sirius.business.internal.helper.task.ExecuteToolOperationTask#createChildrenTasks(ICommandTask, ContainerModelOperation, CommandContext)}.
	 */
	protected void addChildTasks(final AXtextSiriusStyledTextCellEditor xtextSiriusCellEditor,
			final DRepresentationElement representationElement,
			final TransactionalEditingDomain editingDomain,
			final ReplaceValueTask task) {
		final SetValue setValue = extractSetValue(representationElement);
		if (setValue != null) {
			final EObject elementToEdit = representationElement.getTarget();
			final Session session = new EObjectQuery(elementToEdit).getSession();
			final ModelAccessor modelAccessor = SiriusPlugin.getDefault().getModelAccessorRegistry()
					.getModelAccessor(editingDomain.getResourceSet());
			final EMFCommandFactoryUI uiCallback = new EMFCommandFactoryUI();
			final EObject contextEObject = xtextSiriusCellEditor.getModelEntryPoint().getFallbackContainer();
			final CommandContext context = new CommandContext(contextEObject,
					SiriusUtil.findRepresentation(representationElement));
			createChildTasks(task, setValue, context, session, modelAccessor, uiCallback);
		}
	}
	
	private void createChildTasks(final ICommandTask parent, final ContainerModelOperation op,
			final CommandContext context, final Session session, final ModelAccessor modelAccessor,
			final EMFCommandFactoryUI uiCallback) {
		for (final ModelOperation subOp : op.getSubModelOperations()) {
			final AbstractOperationTask task = new ModelOperationToTask(modelAccessor, uiCallback, session, context)
					.createTask(subOp);
			parent.getChildrenTasks().add(task);
			if (!(task instanceof ForTask) && subOp instanceof ContainerModelOperation) {
				createChildTasks(task, (ContainerModelOperation) subOp, context, session, modelAccessor, uiCallback);
			}
		}
	}

	private @Nullable DRepresentationElement extractRepresentationElement() {
		final EditPart host = getHost();
		if (host instanceof IXtextSiriusAwareLabelEditPart) {
			final Object model = ((IXtextSiriusAwareLabelEditPart) host).getModel();
			if (model instanceof View) {
				final EObject element = ((View) model).getElement();
				if (element instanceof DRepresentationElement) {
					return (DRepresentationElement) element;
				}
			}
		}

		return null;
	}

	private @Nullable SetValue extractSetValue(final @Nullable DRepresentationElement representationElement) {
		if (representationElement != null) {
			final RepresentationElementMapping mapping = representationElement.getMapping();
			if (mapping instanceof DiagramElementMapping) {
				final DirectEditLabel labelDirectEdit = ((DiagramElementMapping) mapping).getLabelDirectEdit();
				if (labelDirectEdit != null) {
					final InitialOperation initialOperation = labelDirectEdit.getInitialOperation();
					if (initialOperation != null) {
						final ModelOperation firstModelOperation = initialOperation.getFirstModelOperations();
						if (firstModelOperation instanceof SetValue) {
							return (@Nullable SetValue) firstModelOperation;
						}
					}
				}
			}
		}

		return null;
	}

}
