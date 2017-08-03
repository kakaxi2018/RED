/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.rf.ide.core.execution.agent.event.VariableTypedValue;

public class RobotDebugValueOfDictionary extends RobotDebugValue {

    public static RobotDebugValue create(final RobotDebugVariable parent, final String type,
            final Map<?, ?> map) {
        final List<RobotDebugVariable> nestedVariables = new ArrayList<>();
        for (final Entry<?, ?> entry : map.entrySet()) {
            final VariableTypedValue typeWithValue = (VariableTypedValue) entry.getValue();
            nestedVariables.add(new RobotDebugVariable(parent, entry.getKey().toString(), typeWithValue.getType(),
                    typeWithValue.getValue()));
        }
        final String val = type == null ? "" : type + "[" + nestedVariables.size() + "]";
        return new RobotDebugValueOfDictionary(parent.getDebugTarget(), type, val, nestedVariables);
    }
    

    private final List<RobotDebugVariable> nestedVariables;

    RobotDebugValueOfDictionary(final RobotDebugTarget target, final String type, final String value,
            final List<RobotDebugVariable> nestedVariables) {
        super(target, type, value);
        this.nestedVariables = nestedVariables;
    }

    @Override
    public String getDetailedValue() {
        return Stream.of(getVariables())
                .map(var -> var.getName() + "=" + var.getValue().getDetailedValue())
                .collect(joining(", ", "{", "}"));
    }

    @Override
    public boolean hasVariables() {
        return !nestedVariables.isEmpty();
    }

    @Override
    public RobotDebugVariable[] getVariables() {
        return nestedVariables.toArray(new RobotDebugVariable[0]);
    }

    @Override
    protected void syncValue(final RobotDebugVariable parent, final String type, final Object newValue) {
        final Map<?, ?> newValueDict = (Map<?, ?>) newValue;

        setType(type);
        setValue(type == null ? "" : type + "[" + newValueDict.size() + "]");

        int i = 0;
        for (final Entry<?, ?> entry : newValueDict.entrySet()) {
            final VariableTypedValue typeWithValue = (VariableTypedValue) entry.getValue();

            if (i < nestedVariables.size()) {
                nestedVariables.get(i).syncValue(entry.getKey().toString(), typeWithValue.getType(),
                        typeWithValue.getValue());
            } else {
                nestedVariables.add(new RobotDebugVariable(parent, entry.getKey().toString(), typeWithValue.getType(),
                        typeWithValue.getValue()));
            }
            i++;
        }
        while (i < nestedVariables.size()) {
            nestedVariables.remove(i);
        }
    }

    void setVariables(final Map<String, RobotDebugVariable> variables) {
        nestedVariables.clear();
        nestedVariables.addAll(variables.values());
    }
}
