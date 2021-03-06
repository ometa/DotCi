/*
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.groupon.jenkins.github;

import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.github.services.GithubRepositoryService;

@Extension
public class CommitStatusUpdateListener extends RunListener<DynamicBuild> {

	private static final Logger LOGGER = Logger.getLogger(CommitStatusUpdateListener.class.getName());

	@Override
	public void onStarted(DynamicBuild build, TaskListener listener) {
		GHRepository repository = getGithubRepository(build);
		try {
			repository.createCommitStatus(build.getSha(), GHCommitState.PENDING, build.getFullUrl(), "Build in progress");
		} catch (IOException e) {
			// Ignore if cannot create a pending status
			LOGGER.log(Level.WARNING, "Failed to Update commit status", e);
		}
	}

	protected GHRepository getGithubRepository(DynamicBuild build) {
		return new GithubRepositoryService(build.getGithubRepoUrl()).getGithubRepository();
	}

}
