package com.groupon.jenkins.github.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHDeployKey;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHRepository;

import com.google.common.collect.ImmutableMap;
import com.groupon.jenkins.SetupConfig;

import static com.google.common.collect.Lists.asList;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GithubRepositoryServiceTest {

	private GHRepository githubRepository;
	private SetupConfig setupConfig;
	private GithubAccessTokenRepository githubAccessTokenRepository;
	private GithubRepositoryService githubRepositoryService;

	@Before
	public void setup() {
		githubRepository = mock(GHRepository.class);
		setupConfig = mock(SetupConfig.class);
		githubAccessTokenRepository = mock(GithubAccessTokenRepository.class);
		githubRepositoryService = spy(new GithubRepositoryService(githubRepository, githubAccessTokenRepository));
		doReturn(setupConfig).when(githubRepositoryService).getSetupConfig();
	}

	@Test
	public void should_add_hook_with_the_url_from_dotci_configuration() throws IOException {
		when(setupConfig.getGithubCallbackUrl()).thenReturn("http://jenkins/githook/");

		githubRepositoryService.addHook();

		List<GHEvent> events = Arrays.asList(GHEvent.PUSH, GHEvent.PULL_REQUEST);
		verify(githubRepository).createHook("web", ImmutableMap.of("url", "http://jenkins/githook/"), events, true);
	}

	@Test
	public void should_save_access_tokens_to_database() throws IOException {
		when(setupConfig.getGithubCallbackUrl()).thenReturn("http://jenkins/githook/");
		when(githubRepository.getUrl()).thenReturn("http://github.com/kittah/crunchies");

		githubRepositoryService.addHook();

		verify(githubAccessTokenRepository).put("http://github.com/kittah/crunchies");
	}

	@Test
	public void should_add_traling_slash_if_missing() throws IOException {
		when(setupConfig.getGithubCallbackUrl()).thenReturn("http://jenkins/githook");

		githubRepositoryService.addHook();

		List<GHEvent> events = Arrays.asList(GHEvent.PUSH, GHEvent.PULL_REQUEST);
		verify(githubRepository).createHook("web", ImmutableMap.of("url", "http://jenkins/githook/"), events, true);
	}

	@Test
	public void should_add_deploy_key_from_setup_config_if_private_repo() throws IOException {
		when(githubRepository.isPrivate()).thenReturn(true);
		when(setupConfig.getDeployKey()).thenReturn("deploy_key");

		githubRepositoryService.addDeployKey();

		verify(githubRepository).addDeployKey("DotCi", "deploy_key");

	}

	@Test
	public void should_delete_existing_deploy_key_before_adding_new_one() throws IOException {
		when(githubRepository.isPrivate()).thenReturn(true);
		when(setupConfig.getDeployKey()).thenReturn("deploy_key");
		GHDeployKey deployKey = mock(GHDeployKey.class);
		when(deployKey.getKey()).thenReturn("deploy_key");
		when(githubRepository.getDeployKeys()).thenReturn(Arrays.asList(deployKey));

		githubRepositoryService.addDeployKey();

		verify(deployKey).delete();

	}
}
