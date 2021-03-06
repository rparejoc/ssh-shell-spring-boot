/*
 * Copyright (c) Worldline 2018.
 */

package com.github.fonimus.ssh.shell;

import java.io.File;
import java.io.IOException;

import org.apache.sshd.server.SshServer;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.impl.history.DefaultHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.shell.SpringShellAutoConfiguration;
import org.springframework.shell.jline.JLineShellAutoConfiguration;

import static com.github.fonimus.ssh.shell.SshShellProperties.SSH_SHELL_ENABLE;

/**
 * <p>Ssh shell auto configuration</p>
 * <p>Can be disabled by property <b>ssh.shell.enable=false</b></p>
 */
@Configuration
@ConditionalOnClass(SshServer.class)
@ConditionalOnProperty(name = SSH_SHELL_ENABLE, havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({ SshShellProperties.class })
@AutoConfigureBefore({ JLineShellAutoConfiguration.class, SpringShellAutoConfiguration.class })
public class SshShellHistoryAutoConfiguration {

	public static final String HISTORY_FILE = "historyFile";

	@Bean(HISTORY_FILE)
	public File historyFile(SshShellProperties properties) {
		return properties.getHistoryFile();
	}

	@Configuration
	public static class HistoryConfiguration {

		@Autowired
		@Lazy
		private History history;

		@Bean
		@Primary
		public History history(LineReader lineReader, @Qualifier(HISTORY_FILE) File historyFile) {
			lineReader.setVariable(LineReader.HISTORY_FILE, historyFile.toPath());
			return new DefaultHistory(lineReader);
		}

		@EventListener
		public void onContextClosedEvent(ContextClosedEvent event) throws IOException {
			history.save();
		}
	}
}

