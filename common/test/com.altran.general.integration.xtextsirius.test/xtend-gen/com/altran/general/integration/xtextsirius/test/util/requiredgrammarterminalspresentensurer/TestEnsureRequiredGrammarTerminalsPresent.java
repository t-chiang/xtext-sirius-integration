package com.altran.general.integration.xtextsirius.test.util.requiredgrammarterminalspresentensurer;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.example.fowlerdsl.statemachine.Event;
import org.eclipse.xtext.example.fowlerdsl.statemachine.Statemachine;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionAccess;
import org.eclipse.xtext.util.TextRegion;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Assert;
import org.junit.Test;

import com.altran.general.integration.xtextsirius.test.AFowlerdslDefaultModelTest;

@SuppressWarnings("all")
public class TestEnsureRequiredGrammarTerminalsPresent extends ARequiredGrammarTerminalsPresentEnsurer {
	@Test(expected = IllegalStateException.class)
	public void alreadySet() {
		final StringConcatenation _builder = new StringConcatenation();
		_builder.append("events");
		_builder.newLine();
		_builder.append("\t");
		_builder.append("event1 111");
		_builder.newLine();
		_builder.append("end");
		_builder.newLine();
		final String text = _builder.toString();
		final Statemachine model = this.parseIntoResource(text);
		final Event event = IterableExtensions.<Event> head(model.getEvents());
		final ITextRegionAccess rootRegion = getRootRegion(event);
		final StringBuffer allText = getAllText(rootRegion);
		final EAttribute _event_Name = AFowlerdslDefaultModelTest.statemachineFactory.getStatemachinePackage()
				.getEvent_Name();
		final AccessibleRequiredGrammarTerminalsPresentEnsurer ensurer = new AccessibleRequiredGrammarTerminalsPresentEnsurer(
				event, _event_Name, rootRegion, allText);
		ensurer.ensure();
	}
	
	@Test
	public void noTerminals() {
		final StringConcatenation _builder = new StringConcatenation();
		_builder.append("events");
		_builder.newLine();
		_builder.append("\t");
		_builder.append("event1");
		_builder.newLine();
		_builder.append("end");
		_builder.newLine();
		final String text = _builder.toString();
		final StringConcatenation _builder_1 = new StringConcatenation();
		_builder_1.append("events");
		_builder_1.newLine();
		_builder_1.append("\t");
		_builder_1.append("event1  ");
		_builder_1.newLine();
		_builder_1.append("end");
		_builder_1.newLine();
		final String expectedText = _builder_1.toString();
		final Statemachine model = this.parseIntoResource(text);
		final Event event = IterableExtensions.<Event> head(model.getEvents());
		final ITextRegionAccess rootRegion = getRootRegion(event);
		final StringBuffer allText = getAllText(rootRegion);
		final EAttribute _event_Code = AFowlerdslDefaultModelTest.statemachineFactory.getStatemachinePackage()
				.getEvent_Code();
		final AccessibleRequiredGrammarTerminalsPresentEnsurer ensurer = new AccessibleRequiredGrammarTerminalsPresentEnsurer(
				event, _event_Code, rootRegion, allText);
		final TextRegion resultRegion = ensurer.ensure();
		Assert.assertEquals(expectedText, allText.toString());
		Assert.assertEquals(16, resultRegion.getOffset());
		Assert.assertEquals(0, resultRegion.getLength());
	}
	
	@Test
	public void inBetweenNoTerminals() {
		final StringConcatenation _builder = new StringConcatenation();
		_builder.append("events");
		_builder.newLine();
		_builder.append("\t");
		_builder.append("event1 [123]");
		_builder.newLine();
		_builder.append("end");
		_builder.newLine();
		final String text = _builder.toString();
		final StringConcatenation _builder_1 = new StringConcatenation();
		_builder_1.append("events");
		_builder_1.newLine();
		_builder_1.append("\t");
		_builder_1.append("event1   [123]");
		_builder_1.newLine();
		_builder_1.append("end");
		_builder_1.newLine();
		final String expectedText = _builder_1.toString();
		final Statemachine model = this.parseIntoResource(text);
		final Event event = IterableExtensions.<Event> head(model.getEvents());
		final ITextRegionAccess rootRegion = getRootRegion(event);
		final StringBuffer allText = getAllText(rootRegion);
		final EAttribute _event_Code = AFowlerdslDefaultModelTest.statemachineFactory.getStatemachinePackage()
				.getEvent_Code();
		final AccessibleRequiredGrammarTerminalsPresentEnsurer ensurer = new AccessibleRequiredGrammarTerminalsPresentEnsurer(
				event, _event_Code, rootRegion, allText);
		final TextRegion resultRegion = ensurer.ensure();
		Assert.assertEquals(expectedText, allText.toString());
		Assert.assertEquals(16, resultRegion.getOffset());
		Assert.assertEquals(0, resultRegion.getLength());
	}
	
	@Test
	public void addedTerminals() {
		final StringConcatenation _builder = new StringConcatenation();
		_builder.append("events");
		_builder.newLine();
		_builder.append("\t");
		_builder.append("event1");
		_builder.newLine();
		_builder.append("end");
		_builder.newLine();
		final String text = _builder.toString();
		final StringConcatenation _builder_1 = new StringConcatenation();
		_builder_1.append("events");
		_builder_1.newLine();
		_builder_1.append("\t");
		_builder_1.append("event1[]");
		_builder_1.newLine();
		_builder_1.append("end");
		_builder_1.newLine();
		final String expectedText = _builder_1.toString();
		final Statemachine model = this.parseIntoResource(text);
		final Event event = IterableExtensions.<Event> head(model.getEvents());
		final ITextRegionAccess rootRegion = getRootRegion(event);
		final StringBuffer allText = getAllText(rootRegion);
		final EReference _event_Guard = AFowlerdslDefaultModelTest.statemachineFactory.getStatemachinePackage()
				.getEvent_Guard();
		final AccessibleRequiredGrammarTerminalsPresentEnsurer ensurer = new AccessibleRequiredGrammarTerminalsPresentEnsurer(
				event, _event_Guard, rootRegion, allText);
		final TextRegion resultRegion = ensurer.ensure();
		Assert.assertEquals(expectedText, allText.toString());
		Assert.assertEquals(16, resultRegion.getOffset());
		Assert.assertEquals(0, resultRegion.getLength());
	}
	
	@Test
	public void addedTerminalsOptional() {
		final StringConcatenation _builder = new StringConcatenation();
		_builder.append("events");
		_builder.newLine();
		_builder.append("\t");
		_builder.append("event1 111");
		_builder.newLine();
		_builder.append("end");
		_builder.newLine();
		final String text = _builder.toString();
		final StringConcatenation _builder_1 = new StringConcatenation();
		_builder_1.append("events");
		_builder_1.newLine();
		_builder_1.append("\t");
		_builder_1.append("event1 111[]");
		_builder_1.newLine();
		_builder_1.append("end");
		_builder_1.newLine();
		final String expectedText = _builder_1.toString();
		final Statemachine model = this.parseIntoResource(text);
		final Event event = IterableExtensions.<Event> head(model.getEvents());
		final ITextRegionAccess rootRegion = getRootRegion(event);
		final StringBuffer allText = getAllText(rootRegion);
		final EReference _event_Guard = AFowlerdslDefaultModelTest.statemachineFactory.getStatemachinePackage()
				.getEvent_Guard();
		final AccessibleRequiredGrammarTerminalsPresentEnsurer ensurer = new AccessibleRequiredGrammarTerminalsPresentEnsurer(
				event, _event_Guard, rootRegion, allText);
		final TextRegion resultRegion = ensurer.ensure();
		Assert.assertEquals(expectedText, allText.toString());
		Assert.assertEquals(20, resultRegion.getOffset());
		Assert.assertEquals(0, resultRegion.getLength());
	}
}
