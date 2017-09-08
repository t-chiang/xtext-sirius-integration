package com.altran.general.integration.xtextsirius.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.CompoundElement;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Group;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.formatting2.regionaccess.IEObjectRegion;
import org.eclipse.xtext.formatting2.regionaccess.ISemanticRegion;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionAccess;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.serializer.impl.Serializer;
import org.eclipse.xtext.util.TextRegion;

import com.altran.general.integration.xtextsirius.internal.SemanticElementLocation;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Injector;

@SuppressWarnings("restriction")
public class ModelRegionEditorPreparer {
	@Inject
	private ISerializer serializer;

	private final @Nullable EObject semanticElement;
	private final @NonNull EObject parentSemanticElement;
	private final boolean multiLine;
	private final @NonNull Set<@NonNull String> editableFeatures;
	private final EStructuralFeature semanticElementFeature;
	
	protected boolean prepared;
	
	protected ITextRegionAccess rootRegion;
	protected IEObjectRegion semanticRegion;
	protected Set<@NonNull EStructuralFeature> definedFeatures;
	
	protected StringBuffer allText;
	protected TextRegion textRegion;
	protected SemanticElementLocation semanticElementLocation;
	
	
	public ModelRegionEditorPreparer(
			final @NonNull EObject semanticElement,
			final @NonNull Injector injector,
			final boolean multiLine,
			final @NonNull Collection<@NonNull String> editableFeatures) {
		this(semanticElement, semanticElement.eContainer(), injector, multiLine, editableFeatures,
				semanticElement.eContainingFeature());
	}
	
	public ModelRegionEditorPreparer(
			final @Nullable EObject semanticElement,
			final @NonNull EObject parentSemanticElement,
			final @NonNull Injector injector,
			final boolean multiLine,
			final @NonNull Collection<@NonNull String> editableFeatures,
			final @NonNull EStructuralFeature semanticElementFeature) {
		this.semanticElement = semanticElement;
		this.parentSemanticElement = parentSemanticElement;
		this.multiLine = multiLine;
		this.editableFeatures = Sets.newLinkedHashSet(editableFeatures);
		this.semanticElementFeature = semanticElementFeature;

		injector.injectMembers(this);
	}

	public @NonNull TextRegion getTextRegion() {
		prepare();
		return this.textRegion;
	}

	public @NonNull String getText() {
		prepare();
		return this.allText.toString();
	}

	public @NonNull SemanticElementLocation getSemanticElementLocation() {
		prepare();
		return this.semanticElementLocation;
	}


	public @NonNull String getSemanticText() {
		prepare();
		return this.allText.substring(this.textRegion.getOffset(),
				this.textRegion.getOffset() + this.textRegion.getLength());
	}
	
	protected void prepare() {
		if (this.prepared) {
			return;
		}

		this.rootRegion = getSerializer().serializeToRegions(EcoreUtil.getRootContainer(getParent()));

		this.allText = new StringBuffer(this.rootRegion.regionForDocument().getText());


		final EObject element = getSemanticElement();

		if (element != null) {
			this.semanticElementLocation = new SemanticElementLocation(element);
			this.semanticRegion = this.rootRegion.regionForEObject(element);
			
			if (getEditableFeatures().isEmpty()) {
				this.textRegion = new TextRegion(this.semanticRegion.getOffset(), this.semanticRegion.getLength());
			} else {
				this.definedFeatures = resolveDefinedFeatures(element);

				if (!this.definedFeatures.isEmpty()) {
					this.textRegion = calculateRegionForFeatures(element);
				} else {
					this.textRegion = ensureRequiredGrammarTerminalsPresent(element,
							resolveEditableFeatures(element).iterator().next());
				}
			}
		} else {
			this.semanticElementLocation = constructXtextFragmentSchemeBasedLocation();
			this.semanticRegion = this.rootRegion.regionForEObject(getParent());
			this.textRegion = ensureRequiredGrammarTerminalsPresent(getParent(), getSemanticElementFeature());
		}

		this.textRegion = StyledTextUtil.getInstance().insertNewline(this.allText, this.textRegion);
		
		StyledTextUtil.getInstance().removeNewlinesIfSingleLine(this.allText, this.textRegion, isMultiLine());

		this.prepared = true;
	}
	
	protected SemanticElementLocation constructXtextFragmentSchemeBasedLocation() {
		final EStructuralFeature feature = getSemanticElementFeature();
		final String parentFragment = EcoreUtil.getURI(getParent()).fragment();
		final String fragment = parentFragment + "/@" + feature.getName() + (feature.isMany() ? ".1" : "");
		return new SemanticElementLocation(fragment, parentFragment, feature, 0);
	}


	protected @NonNull TextRegion ensureRequiredGrammarTerminalsPresent(
			final @NonNull EObject element,
			final @NonNull EStructuralFeature feature) {
		if (element.eIsSet(feature)) {
			throw new IllegalStateException("Feature " + feature + " is set in " + element);
		}
		
		final IEObjectRegion elementRegion = this.rootRegion.regionForEObject(element);
		
		if (!(elementRegion.getGrammarElement() instanceof RuleCall)) {
			throw new IllegalArgumentException("element does not resolve to RuleCall grammar element: " + element);
		}

		final RuleCall grammarElement = (RuleCall) elementRegion.getGrammarElement();
		
		final List<@NonNull AbstractElement> containedElementPath = findContainedElementPath(grammarElement, feature);
		
		if (containedElementPath.isEmpty()) {
			throw new IllegalArgumentException("Cannot find grammar element for feature " + feature + " in " + element);
		}
		
		final AbstractElement containedElement = Iterables.getLast(containedElementPath);
		final Group containingGroup = GrammarUtil.containingGroup(containedElement);
		// 0-th entry must be == grammarElement, so we don't need it
		containedElementPath.remove(0);
		
		if (containingGroup == null) {
			throw new IllegalArgumentException(
					"Cannot find containing group for grammar element of feature " + feature + " in " + element);
		}
		
		final List<AbstractElement> elementsBefore = Lists.newArrayList();
		final List<AbstractElement> elementsAfter = Lists.newArrayList();
		collectGrammarElementsBeforeAndAfter(containedElement, containingGroup, elementsBefore, elementsAfter);
		
		final String beforeText = collectToTerminalText(grammarElement, elementsBefore);
		final String afterText = collectToTerminalText(grammarElement, elementsAfter);
		
		final Multimap<@NonNull AbstractElement, @NonNull AbstractElement> parentMap = collectContainedGrammarElementsDeep(
				grammarElement, grammarElement, LinkedHashMultimap.create());
		
		final Set<@NonNull ISemanticRegion> regionsOfContainedElements = findRegionsOfContainedElements(elementRegion,
				containedElementPath, parentMap);
		
		ISemanticRegion max;
		// this is probably only a workaround, but it works for the current test
		// cases and abstract reasoning about possible grammars and token
		// streams is hard /-:
		if (regionsOfContainedElements.size() == 1) {
			max = regionsOfContainedElements.iterator().next();
		} else {
			final Set<@NonNull ISemanticRegion> regionsBefore = regionsOfContainedElements.stream()
					.filter(r -> !containsGrammarElementDeep((AbstractElement) r.getGrammarElement(), elementsAfter,
							parentMap))
					.collect(Collectors.toSet());
			max = selectLastmostRegion(regionsBefore);
		}
		
		final int endOffset = max.getEndOffset();
		
		this.allText.insert(endOffset, afterText);
		this.allText.insert(endOffset, beforeText);
		
		return new TextRegion(endOffset + beforeText.length(), 0);
	}
	
	protected String getWhitespace(final EObject grammarElement) {
		return EcoreUtil2
				.getAllContentsOfType(GrammarUtil.findRuleForName(GrammarUtil.getGrammar(grammarElement), "WS"),
						Keyword.class)
				.iterator().next().getValue();
	}
	
	protected ISemanticRegion selectLastmostRegion(
			final @NonNull Set<@NonNull ISemanticRegion> regionsOfContainedElements) {
		final ISemanticRegion max = regionsOfContainedElements.stream()
				.max((a, b) -> Integer.compare(a.getEndOffset(), b.getEndOffset()))
				.get();
		return max;
	}
	
	protected @NonNull Set<@NonNull ISemanticRegion> findRegionsOfContainedElements(
			final @NonNull IEObjectRegion elementRegion,
			final @NonNull List<@NonNull AbstractElement> containedElementPath,
			final @NonNull Multimap<@NonNull AbstractElement, @NonNull AbstractElement> parentMap) {

		final Set<@NonNull ISemanticRegion> result = Sets.newLinkedHashSet();

		final EObject grammarElement = elementRegion.getGrammarElement();
		if (grammarElement instanceof AbstractElement) {
			for (final ISemanticRegion region : elementRegion.getAllSemanticRegions()) {
				final EObject regionGrammarElement = region.getGrammarElement();
				if (regionGrammarElement instanceof AbstractElement) {
					if (containsGrammarElementDeep((AbstractElement) regionGrammarElement, containedElementPath,
							parentMap)) {
						result.add(region);
					}
				}
			}
		}

		return result;
	}

	protected @NonNull Multimap<@NonNull AbstractElement, @NonNull AbstractElement> collectContainedGrammarElementsDeep(
			final @NonNull AbstractElement parent,
			final @NonNull AbstractElement base,
			final @NonNull Multimap<@NonNull AbstractElement, @NonNull AbstractElement> map) {
		if (map.containsEntry(base, parent)) {
			return map;
		}

		map.put(base, parent);

		if (base instanceof RuleCall) {
			collectContainedGrammarElementsDeep(base, ((RuleCall) base).getRule().getAlternatives(), map);
		} else if (base instanceof Assignment) {
			collectContainedGrammarElementsDeep(base, ((Assignment) base).getTerminal(), map);
		} else if (base instanceof CrossReference) {
			collectContainedGrammarElementsDeep(base, ((CrossReference) base).getTerminal(), map);
		} else if (base instanceof CompoundElement) {
			for (final AbstractElement element : ((CompoundElement) base).getElements()) {
				collectContainedGrammarElementsDeep(base, element, map);
			}
		}

		return map;
	}

	protected boolean containsGrammarElementDeep(
			final @NonNull AbstractElement grammarElement,
			final @NonNull List<@NonNull AbstractElement> grammarElements,
			final @NonNull Multimap<@NonNull AbstractElement, @NonNull AbstractElement> parentMap) {
		if (grammarElements.contains(grammarElement)) {
			return true;
		}

		for (final AbstractElement parent : parentMap.get(grammarElement)) {
			if (parent != null && parent != grammarElement) {
				return containsGrammarElementDeep(parent, grammarElements, parentMap);
			}
		}

		return false;
	}
	
	protected void collectGrammarElementsBeforeAndAfter(
			final @NonNull AbstractElement containedElement,
			final @NonNull Group containingGroup,
			final @NonNull List<@NonNull AbstractElement> elementsBefore,
			final @NonNull List<@NonNull AbstractElement> elementsAfter) {
		List<AbstractElement> currentList = elementsBefore;
		
		for (final AbstractElement ae : containingGroup.getElements()) {
			if (ae == containedElement
					|| EcoreUtil2.eAllContentsAsList(ae).contains(containedElement)) {
				currentList = elementsAfter;
			} else {
				currentList.add(ae);
			}
		}
	}

	protected @NonNull String collectToTerminalText(final @NonNull AbstractElement grammarElement,
			final @NonNull List<@NonNull AbstractElement> grammarElements) {
		final String result = grammarElements.stream()
				.filter(e -> e instanceof Keyword)
				.map(el -> ((Keyword) el).getValue())
				.collect(Collectors.joining());
		
		if (!result.isEmpty()) {
			return result;
		}
		
		return getWhitespace(grammarElement);
	}

	protected @NonNull List<@NonNull AbstractElement> findContainedElementPath(
			final @NonNull AbstractElement abstractElement,
			final @NonNull EStructuralFeature feature) {
		if (abstractElement instanceof Assignment) {
			if (feature.getName().equals(((Assignment) abstractElement).getFeature())) {
				return Collections.singletonList(abstractElement);
			}
		}

		if (abstractElement instanceof RuleCall) {
			final AbstractElement alternatives = ((RuleCall) abstractElement).getRule().getAlternatives();

			final List<AbstractElement> alternativesResult = findContainedElementPath(alternatives, feature);
			if (!alternativesResult.isEmpty()) {
				final ArrayList<AbstractElement> result = Lists.newArrayList(alternativesResult);
				result.add(0, abstractElement);
				return result;
			}
		}

		if (abstractElement instanceof CompoundElement) {
			for (final AbstractElement alternative : ((CompoundElement) abstractElement).getElements()) {
				final List<AbstractElement> alternativeResult = findContainedElementPath(alternative, feature);
				if (!alternativeResult.isEmpty()) {
					final ArrayList<AbstractElement> result = Lists.newArrayList(alternativeResult);
					result.add(0, abstractElement);
					return result;
				}
			}
		}

		return Collections.emptyList();
	}

	protected @NonNull TextRegion calculateRegionForFeatures(final @NonNull EObject semanticElement) {
		final Set<@NonNull ISemanticRegion> featureRegions = translateToRegions(this.definedFeatures,
				this.semanticRegion,
				semanticElement, this.rootRegion);
		
		final int startOffset = featureRegions.stream()
				.map(reg -> reg.getOffset())
				.min(Integer::compare)
				.get();
		
		final int endOffset = featureRegions.stream()
				.map(reg -> {
					final ISemanticRegion nextHiddenRegion = reg.getNextSemanticRegion();
					if (nextHiddenRegion.getGrammarElement() instanceof Keyword) {
						return nextHiddenRegion.getEndOffset();
					}
					return reg.getEndOffset();
				})
				.max(Integer::compare)
				.get();

		return new TextRegion(startOffset, endOffset - startOffset);
	}
	
	protected @NonNull Set<@NonNull EStructuralFeature> resolveDefinedFeatures(final @NonNull EObject semanticElement) {
		final @NonNull Set<@NonNull EStructuralFeature> features = resolveEditableFeatures(semanticElement);
		final @NonNull Set<@NonNull EStructuralFeature> definedFeatures = features.stream()
				.filter(feature -> semanticElement.eIsSet(feature))
				.collect(Collectors.toSet());
		return definedFeatures;
	}
	
	protected @NonNull Set<@NonNull ISemanticRegion> translateToRegions(
			final @NonNull Set<@NonNull EStructuralFeature> features,
			final @NonNull IEObjectRegion semanticRegion,
			final @NonNull EObject semanticElement,
			final @NonNull ITextRegionAccess rootRegion) {
		return features.stream()
				.flatMap(feature -> {
					if (canBeHandledByGetRegionForFeature(feature)) {
						return Stream.of(semanticRegion.getRegionFor().feature(feature));
					} else {
						final Object child = semanticElement.eGet(feature);
						if (child instanceof EObject) {
							return asStream(rootRegion.regionForEObject((EObject) child).getAllSemanticRegions());
						} else {
							return Stream.of();
						}
					}
				})
				.collect(Collectors.toSet());
	}

	/*
	 * Inverted version of org.eclipse.xtext.formatting2.regionaccess.internal.
	 * AbstractSemanticRegionsFinder#assertNoContainment(EStructuralFeature)
	 */
	protected boolean canBeHandledByGetRegionForFeature(final @NonNull EStructuralFeature feature) {
		return feature instanceof EAttribute
				|| (feature instanceof EReference && !((EReference) feature).isContainment());
	}
	
	protected @NonNull Set<@NonNull EStructuralFeature> resolveEditableFeatures(
			final @NonNull EObject semanticElement) {
		final EClass eClass = semanticElement.eClass();
		
		return getEditableFeatures().stream()
				.map(ef -> eClass.getEStructuralFeature(ef))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}
	
	private <T> @NonNull Stream<T> asStream(final @NonNull Iterable<T> iter) {
		return StreamSupport.stream(iter.spliterator(), false);
	}
	
	
	protected EObject getSemanticElement() {
		return this.semanticElement;
	}
	
	protected boolean isMultiLine() {
		return this.multiLine;
	}

	protected EObject getParent() {
		return this.parentSemanticElement;
	}
	
	protected @NonNull Set<@NonNull String> getEditableFeatures() {
		return this.editableFeatures;
	}
	
	protected EStructuralFeature getSemanticElementFeature() {
		return this.semanticElementFeature;
	}
	
	protected Serializer getSerializer() {
		return (Serializer) this.serializer;
	}
}
