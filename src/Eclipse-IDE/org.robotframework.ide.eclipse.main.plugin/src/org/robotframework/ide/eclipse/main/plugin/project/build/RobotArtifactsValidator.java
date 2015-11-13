/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Queue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;
import org.robotframework.ide.eclipse.main.plugin.project.RobotSuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotInitFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotProjectConfigFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotResourceFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotSuiteFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext;

import com.google.common.base.Optional;
import com.google.common.collect.Queues;

public class RobotArtifactsValidator {

    private final IProject project;

    public RobotArtifactsValidator(final IProject project) {
        this.project = project;
    }

    public static void revalidate(final RobotSuiteFile suiteModel) {
        final IFile file = suiteModel.getFile();
        if (file == null || !file.exists() || !RobotProjectNature.hasRobotNature(file.getProject())) {
            return;
        }

        final Optional<? extends ModelUnitValidator> validator = RobotArtifactsValidator
                .createProperValidator(ValidationContext.createFor(file), file);

        if (validator.isPresent()) {
            final WorkspaceJob wsJob = new WorkspaceJob("Revalidating model") {

                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                    file.deleteMarkers(RobotProblem.TYPE_ID, true, 1);
                    ((RobotFileValidator) validator.get()).validate(suiteModel, new NullProgressMonitor());

                    return Status.OK_STATUS;
                }
            };
            wsJob.setSystem(true);
            wsJob.schedule();
        }
    }

    public Job createValidationJob(final IResourceDelta delta, final int kind) {
        return new Job("Validating") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                try {
                    final Queue<ModelUnitValidator> unitValidators = Queues.newArrayDeque();

                    if (delta == null || kind == IncrementalProjectBuilder.FULL_BUILD) {
                        unitValidators.addAll(createValidationUnitsForWholeProject());
                    } else if (delta != null) {
                        unitValidators.addAll(createValidationUnitsForChangedFiles(delta));
                    }

                    final SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
                    subMonitor.beginTask("Validating files", 100);
                    subMonitor.setWorkRemaining(unitValidators.size());

                    while (!unitValidators.isEmpty()) {
                        if (monitor.isCanceled()) {
                            monitor.setCanceled(true);
                            break;
                        }
                        final ModelUnitValidator validator = unitValidators.poll();
                        validator.validate(monitor);
                        subMonitor.worked(1);
                    }

                    return Status.OK_STATUS;
                } catch (final CoreException e) {
                    RedPlugin.logError("Project validation was corrupted", e);
                    return Status.CANCEL_STATUS;
                } finally {
                    monitor.done();
                }
            }
        };
    }

    private List<ModelUnitValidator> createValidationUnitsForWholeProject()
            throws CoreException {
        final List<ModelUnitValidator> validators = newArrayList();
        validators.add(new ModelUnitValidator() {
            @Override
            public void validate(final IProgressMonitor monitor) throws CoreException {
                project.deleteMarkers(RobotProblem.TYPE_ID, true, IResource.DEPTH_INFINITE);
            }
        });
        project.accept(new IResourceVisitor() {
            @Override
            public boolean visit(final IResource resource) throws CoreException {
                final Optional<? extends ModelUnitValidator> validationUnit = createValidationUnits(resource);
                if (validationUnit.isPresent()) {
                    final ModelUnitValidator unit = validationUnit.get();
                    validators.add(unit);
                }
                return true;
            }
        });
        return validators;
    }

    private List<ModelUnitValidator> createValidationUnitsForChangedFiles(final IResourceDelta delta)
            throws CoreException {
        final List<ModelUnitValidator> validators = newArrayList();
        delta.accept(new IResourceDeltaVisitor() {

            @Override
            public boolean visit(final IResourceDelta delta) throws CoreException {
                if (delta.getKind() != IResourceDelta.REMOVED && (delta.getFlags() & IResourceDelta.CONTENT) != 0) {
                    final IResource file = delta.getResource();
                    final Optional<? extends ModelUnitValidator> validationUnit = createValidationUnits(file);
                    if (validationUnit.isPresent()) {
                        validators.add(new ModelUnitValidator() {
                            @Override
                            public void validate(final IProgressMonitor monitor) throws CoreException {
                                file.deleteMarkers(RobotProblem.TYPE_ID, true, 1);
                                validationUnit.get().validate(monitor);
                            }
                        });
                    }
                }
                return true;
            }
        });
        return validators;
    }

    private Optional<? extends ModelUnitValidator> createValidationUnits(final IResource resource)
            throws CoreException {
        if (resource.getType() != IResource.FILE) {
            return Optional.absent();
        }
        final IFile file = (IFile) resource;
        final ValidationContext validationContext = ValidationContext.createFor(file);
        return createProperValidator(validationContext, file);
    }

    public static Optional<? extends ModelUnitValidator> createProperValidator(
            final ValidationContext validationContext, final IFile file) {
        if (RobotSuiteFileDescriber.isSuiteFile(file)) {
            return Optional.of(new RobotSuiteFileValidator(validationContext, file));
        } else if (RobotSuiteFileDescriber.isResourceFile(file)) {
            return Optional.of(new RobotResourceFileValidator(validationContext, file));
        } else if (RobotSuiteFileDescriber.isInitializationFile(file)) {
            return Optional.of(new RobotInitFileValidator(validationContext, file));
        } else if (file.getName().equals("red.xml") && file.getParent() == file.getProject()) {
            return Optional.of(new RobotProjectConfigFileValidator(validationContext, file));
        }
        return Optional.absent();
    }

    public interface ModelUnitValidator {

        void validate(IProgressMonitor monitor) throws CoreException;
    }
}
