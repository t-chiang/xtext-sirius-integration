package com.altran.general.integration.xtextsirius.model.test.emerger.ignored;

import com.altran.general.integration.xtextsirius.model.test.XtextSiriusTest.Element;
import com.altran.general.integration.xtextsirius.model.test.XtextSiriusTest.XtextSiriusTestPackage;
import com.altran.general.integration.xtextsirius.model.test.emerger.TestEMergerContainment;
import com.altran.general.integration.xtextsirius.util.EMerger;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import java.util.Collections;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestEMergerContainmentIgnoredNestedFeatures extends TestEMergerContainment {
  private int i = 100;
  
  protected Element existing;
  
  protected Set<String> nestedFeaturesToIgnore = IterableExtensions.<String>toSet(Iterables.<String>concat(ListExtensions.<EStructuralFeature, Iterable<String>>map(XtextSiriusTestPackage.Literals.ELEMENT.getEAllStructuralFeatures(), ((Function1<EStructuralFeature, Iterable<String>>) (EStructuralFeature feature) -> {
    final Function1<EStructuralFeature, Boolean> _function = (EStructuralFeature it) -> {
      String _name = it.getName();
      return Boolean.valueOf((!Objects.equal(_name, "changeableAttr")));
    };
    final Function1<EStructuralFeature, String> _function_1 = (EStructuralFeature it) -> {
      String _name = feature.getName();
      String _plus = (_name + ".");
      String _name_1 = it.getName();
      return (_plus + _name_1);
    };
    return IterableExtensions.<EStructuralFeature, String>map(IterableExtensions.<EStructuralFeature>filter(XtextSiriusTestPackage.Literals.ELEMENT.getEAllStructuralFeatures(), _function), _function_1);
  }))));
  
  @Override
  protected EMerger<Element> createEMerger(final Element existing, final Element edited) {
    EMerger<Element> _xblockexpression = null;
    {
      this.existing = existing;
      Set<String> _emptySet = CollectionLiterals.<String>emptySet();
      URI _createURI = URI.createURI("resourceName.xmi#/42");
      _xblockexpression = new EMerger<Element>(existing, _emptySet, this.nestedFeaturesToIgnore, _createURI);
    }
    return _xblockexpression;
  }
  
  @Override
  protected EMerger<Element> createEMerger(final Element existing, final EStructuralFeature feature) {
    EMerger<Element> _xblockexpression = null;
    {
      this.existing = existing;
      Set<String> _emptySet = CollectionLiterals.<String>emptySet();
      URI _createURI = URI.createURI("resourceName.xmi#/42");
      _xblockexpression = new EMerger<Element>(existing, _emptySet, this.nestedFeaturesToIgnore, _createURI);
    }
    return _xblockexpression;
  }
  
  @After
  public void checkIgnoredNestedFeatures() {
    Assert.assertNotNull(this.existing);
    final Procedure1<Element> _function = (Element it) -> {
      Assert.assertTrue(it.getChangeableBagAttr().isEmpty());
      Assert.assertTrue(it.getChangeableBagRef().isEmpty());
      Assert.assertNull(it.getChangeableCont());
      Assert.assertTrue(it.getChangeableListAttr().contains("aaa"));
      Assert.assertTrue(it.getChangeableListAttr().contains("bbb"));
      Assert.assertTrue(it.getChangeableListRef().isEmpty());
      Assert.assertNull(it.getChangeableRef());
      Assert.assertTrue(it.getChangeableSetAttr().isEmpty());
      Assert.assertTrue(it.getChangeableSetCont().isEmpty());
      Assert.assertTrue(it.getChangeableSetRef().isEmpty());
      Assert.assertTrue(it.getChangeableUniqueListCont().isEmpty());
    };
    IteratorExtensions.<Element>forEach(Iterators.<Element>filter(EcoreUtil.<Object>getAllContents(this.existing, false), Element.class), _function);
  }
  
  @Test
  @Override
  public void singleNonNull_singleExisting() {
    Element _createRootElement = this.createRootElement();
    final Procedure1<Element> _function = (Element it) -> {
      it.setChangeableCont(this.newEdited(1, "answer"));
    };
    final Element edited = ObjectExtensions.<Element>operator_doubleArrow(_createRootElement, _function);
    Element _createRootElement_1 = this.createRootElement();
    final Procedure1<Element> _function_1 = (Element it) -> {
      Element _newExisting = this.newExisting(1, "question");
      final Procedure1<Element> _function_2 = (Element it_1) -> {
        EList<String> _changeableListAttr = it_1.getChangeableListAttr();
        Iterables.<String>addAll(_changeableListAttr, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("aaa", "bbb")));
      };
      Element _doubleArrow = ObjectExtensions.<Element>operator_doubleArrow(_newExisting, _function_2);
      it.setChangeableCont(_doubleArrow);
    };
    final Element existing = ObjectExtensions.<Element>operator_doubleArrow(_createRootElement_1, _function_1);
    final Element result = this.createEMerger(existing, edited).merge(edited);
    Assert.assertEquals("aanswer", result.getChangeableCont().getChangeableAttr());
    Assert.assertTrue(result.getChangeableCont().getChangeableListAttr().contains("aaa"));
    Assert.assertTrue(result.getChangeableCont().getChangeableListAttr().contains("bbb"));
  }
  
  @Test
  @Override
  public void singleNonNull_singleNew() {
    Element _createRootElement = this.createRootElement();
    final Procedure1<Element> _function = (Element it) -> {
      Element _newEdited = this.newEdited(1, "answer");
      final Procedure1<Element> _function_1 = (Element it_1) -> {
        EList<String> _changeableListAttr = it_1.getChangeableListAttr();
        _changeableListAttr.add("ccc");
      };
      Element _doubleArrow = ObjectExtensions.<Element>operator_doubleArrow(_newEdited, _function_1);
      it.setChangeableCont(_doubleArrow);
    };
    final Element edited = ObjectExtensions.<Element>operator_doubleArrow(_createRootElement, _function);
    Element _createRootElement_1 = this.createRootElement();
    final Procedure1<Element> _function_1 = (Element it) -> {
      it.setChangeableCont(null);
    };
    final Element existing = ObjectExtensions.<Element>operator_doubleArrow(_createRootElement_1, _function_1);
    final Element result = this.createEMerger(existing, edited).merge(edited);
    Assert.assertNotNull(result.getChangeableCont());
    Assert.assertEquals("aanswer", result.getChangeableCont().getChangeableAttr());
    Assert.assertFalse(result.getChangeableCont().getChangeableListAttr().contains("bbb"));
    EList<String> _changeableListAttr = result.getChangeableCont().getChangeableListAttr();
    Iterables.<String>addAll(_changeableListAttr, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("aaa", "bbb")));
  }
  
  @Override
  protected Element newEdited(final int id, final String attrValue) {
    Element _createRootElement = this.createRootElement();
    final Procedure1<Element> _function = (Element it) -> {
      it.setChangeableAttr(("a" + attrValue));
      EList<Double> _changeableBagAttr = it.getChangeableBagAttr();
      Iterables.<Double>addAll(_changeableBagAttr, Collections.<Double>unmodifiableList(CollectionLiterals.<Double>newArrayList(Double.valueOf(2.71), Double.valueOf(3.14))));
      EList<Element> _changeableBagRef = it.getChangeableBagRef();
      Element _newDummyElement = this.newDummyElement();
      Element _newDummyElement_1 = this.newDummyElement();
      Iterables.<Element>addAll(_changeableBagRef, Collections.<Element>unmodifiableList(CollectionLiterals.<Element>newArrayList(_newDummyElement, _newDummyElement_1)));
      it.setChangeableCont(this.newDummyElement());
      EList<String> _changeableListAttr = it.getChangeableListAttr();
      Iterables.<String>addAll(_changeableListAttr, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("2.71", "3.14")));
      EList<Element> _changeableListRef = it.getChangeableListRef();
      Element _newDummyElement_2 = this.newDummyElement();
      Element _newDummyElement_3 = this.newDummyElement();
      Iterables.<Element>addAll(_changeableListRef, Collections.<Element>unmodifiableList(CollectionLiterals.<Element>newArrayList(_newDummyElement_2, _newDummyElement_3)));
      it.setChangeableRef(this.newDummyElement());
      EList<Integer> _changeableSetAttr = it.getChangeableSetAttr();
      Iterables.<Integer>addAll(_changeableSetAttr, Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(2), Integer.valueOf(3))));
      EList<Element> _changeableSetCont = it.getChangeableSetCont();
      Element _newDummyElement_4 = this.newDummyElement();
      Element _newDummyElement_5 = this.newDummyElement();
      Iterables.<Element>addAll(_changeableSetCont, Collections.<Element>unmodifiableList(CollectionLiterals.<Element>newArrayList(_newDummyElement_4, _newDummyElement_5)));
      EList<Element> _changeableSetRef = it.getChangeableSetRef();
      Element _newDummyElement_6 = this.newDummyElement();
      Element _newDummyElement_7 = this.newDummyElement();
      Iterables.<Element>addAll(_changeableSetRef, Collections.<Element>unmodifiableList(CollectionLiterals.<Element>newArrayList(_newDummyElement_6, _newDummyElement_7)));
      EList<Element> _changeableUniqueListCont = it.getChangeableUniqueListCont();
      Element _newDummyElement_8 = this.newDummyElement();
      Element _newDummyElement_9 = this.newDummyElement();
      Iterables.<Element>addAll(_changeableUniqueListCont, Collections.<Element>unmodifiableList(CollectionLiterals.<Element>newArrayList(_newDummyElement_8, _newDummyElement_9)));
    };
    final Element result = ObjectExtensions.<Element>operator_doubleArrow(_createRootElement, _function);
    EList<EObject> _contents = this.editedResource.getContents();
    _contents.add(result);
    return result;
  }
  
  @Override
  protected Element newExisting(final int id, final String attrValue) {
    Element _newExisting = super.newExisting(id, attrValue);
    final Procedure1<Element> _function = (Element it) -> {
      EList<String> _changeableListAttr = it.getChangeableListAttr();
      Iterables.<String>addAll(_changeableListAttr, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("aaa", "bbb")));
    };
    return ObjectExtensions.<Element>operator_doubleArrow(_newExisting, _function);
  }
  
  private Element newDummyElement() {
    int _plusPlus = this.i++;
    String _plus = ("untouched" + Integer.valueOf(_plusPlus));
    return this.newElement(this.i, _plus);
  }
}
