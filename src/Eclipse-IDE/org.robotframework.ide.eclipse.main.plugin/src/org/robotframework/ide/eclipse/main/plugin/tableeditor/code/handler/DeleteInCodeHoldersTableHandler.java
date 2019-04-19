/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.EmptyCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeTableValuesChangingCommandsCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.DeleteInCodeHoldersTableHandler.E4DeleteInCodeHoldersTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteInCodeHoldersTableHandler extends DIParameterizedHandler<E4DeleteInCodeHoldersTableHandler> {

    public DeleteInCodeHoldersTableHandler() {
        super(E4DeleteInCodeHoldersTableHandler.class);
    }

    public static class E4DeleteInCodeHoldersTableHandler {

        @Execute
        public void delete(@Named(Selections.SELECTION) final IStructuredSelection selection,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                final RobotEditorCommandsStack commandsStack) {

            final List<RobotElement> elements = Selections.getElements(selection, RobotElement.class);
            if (!elements.isEmpty()) {
                final List<EditorCommand> detailsDeletingCommands = createCommandsForDetailsRemoval(elements,
                        editor.getSelectionLayerAccessor());
                Collections.reverse(detailsDeletingCommands); // deleting must be started from the
                                                              // biggest column index

                final EditorCommand parentCommand = new EmptyCommand();
                for (final EditorCommand command : detailsDeletingCommands) {
                    command.setParent(parentCommand);
                    commandsStack.execute(command);
                }
            }
        }

        private List<EditorCommand> createCommandsForDetailsRemoval(final List<RobotElement> elements,
                final SelectionLayerAccessor selectionLayerAccessor) {
            final List<EditorCommand> commands = new ArrayList<>();
            final PositionCoordinate[] selectedCellPositions = selectionLayerAccessor.getSelectedPositions();
            if (selectedCellPositions.length == 0) {
                return commands;
            }

            int selectedElemRow = 0;
            for (final RobotElement element : elements) {
                selectedElemRow = selectionLayerAccessor.findNextSelectedElementRowIndex(selectedElemRow);

                final List<Integer> selectedColumnsIndexes = selectionLayerAccessor
                        .findSelectedColumnsIndexesByRowIndex(selectedElemRow);

                new CodeTableValuesChangingCommandsCollector()
                        .collectForRemoval(element, selectedColumnsIndexes)
                        .forEach(commands::add);
            }
            return commands;
        }
    }
}
