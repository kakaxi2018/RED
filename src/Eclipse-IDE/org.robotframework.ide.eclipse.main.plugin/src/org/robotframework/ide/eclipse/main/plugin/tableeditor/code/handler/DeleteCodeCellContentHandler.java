/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.DeleteCodeCellContentHandler.E4DeleteCodeCellContentHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.E4DeleteCellContentHandler;

import com.google.common.base.Optional;

public class DeleteCodeCellContentHandler extends DIHandler<E4DeleteCodeCellContentHandler> {

    public DeleteCodeCellContentHandler() {
        super(E4DeleteCodeCellContentHandler.class);
    }

    public static class E4DeleteCodeCellContentHandler extends E4DeleteCellContentHandler {
        @Override
        protected Optional<? extends EditorCommand> provideCommandForAttributeChange(final RobotElement element,
                final int index, final int noOfColumns) {
            return new CodeAttributesCommandsProvider().provideChangeAttributeCommand(element, index, noOfColumns, "");
        }
    }
}
