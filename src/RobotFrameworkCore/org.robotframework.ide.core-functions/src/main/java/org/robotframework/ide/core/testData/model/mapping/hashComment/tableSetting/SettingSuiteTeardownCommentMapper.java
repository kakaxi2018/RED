/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.setting.SuiteTeardown;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class SettingSuiteTeardownCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_SUITE_TEARDOWN
                || state == ParsingState.SETTING_SUITE_TEARDOWN_KEYWORD || state == ParsingState.SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        List<SuiteTeardown> suiteTeardowns = fileModel.getSettingTable()
                .getSuiteTeardowns();
        if (!suiteTeardowns.isEmpty()) {
            SuiteTeardown suiteTeardown = suiteTeardowns.get(suiteTeardowns
                    .size() - 1);
            suiteTeardown.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }

}
