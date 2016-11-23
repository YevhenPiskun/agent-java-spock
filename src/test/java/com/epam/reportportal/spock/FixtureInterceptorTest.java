package com.epam.reportportal.spock;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.ErrorInfo;
import org.spockframework.runtime.model.MethodInfo;

/**
 * Created by Dzmitry_Mikhievich
 */
@RunWith(MockitoJUnitRunner.class)
public class FixtureInterceptorTest {

	private static final MethodInfo testFixture = new MethodInfo();

	@Mock
	private IMethodInvocation invocationMock;
	@Mock
	private ISpockReporter spockReporterMock;
	@InjectMocks
	private FixtureInterceptor fixtureInterceptor;

	@Before
	public void configureMocks() {
		when(invocationMock.getMethod()).thenReturn(testFixture);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_spockReporterIsNull() {
		new FixtureInterceptor(null);
	}

	@Test
	public void testIntercept_whenMethodInvocationThrowsException_fixtureShouldBeReportedCorrectly() throws Throwable {
		doThrow(TestException.class).when(invocationMock).proceed();

		try {
			fixtureInterceptor.intercept(invocationMock);
		} catch (Throwable ex) {
		}

		InOrder inOrder = inOrder(spockReporterMock);
		inOrder.verify(spockReporterMock, times(1)).registerFixture(testFixture);
		inOrder.verify(spockReporterMock, times(1)).reportError(Mockito.<ErrorInfo> any());
		inOrder.verify(spockReporterMock, times(1)).publishFixtureResult(testFixture);
	}

	@Test(expected = TestException.class)
	public void testIntercept_whenMethodInvocationThrowsException_exceptionShouldBeThrownAgain() throws Throwable {
		doThrow(TestException.class).when(invocationMock).proceed();

		fixtureInterceptor.intercept(invocationMock);
	}

	@Test
	public void testIntercept_methodInvocationProceedSuccessfully() throws Throwable {
		fixtureInterceptor.intercept(invocationMock);

		InOrder inOrder = inOrder(spockReporterMock);
		inOrder.verify(spockReporterMock, times(1)).registerFixture(testFixture);
		inOrder.verify(spockReporterMock, times(1)).publishFixtureResult(testFixture);
		verify(spockReporterMock, times(0)).reportError(Mockito.<ErrorInfo> any());
	}

	private static class TestException extends Throwable {
	}
}