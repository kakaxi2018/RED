/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.testcases;

import java.util.regex.Pattern;

import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class TestCaseTeardownRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern
            .compile("[ ]?((\\[\\s*" + createUpperLowerCaseWord("Teardown") + "\\s*\\]))");

    public TestCaseTeardownRecognizer() {
        super(EXPECTED, RobotTokenType.TEST_CASE_SETTING_TEARDOWN);
    }

    @Override
    public ATokenRecognizer newInstance() {
        return new TestCaseTeardownRecognizer();
    }
}
