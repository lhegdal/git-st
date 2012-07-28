package com.googlecode.gitst;

import static com.googlecode.gitst.RepoProperties.PROP_CA;
import static com.googlecode.gitst.RepoProperties.PROP_DEFAULT_IGNORE;
import static com.googlecode.gitst.RepoProperties.PROP_DEFAULT_THREADS;
import static com.googlecode.gitst.RepoProperties.PROP_DEFAULT_USER_PATTERN;
import static com.googlecode.gitst.RepoProperties.PROP_FETCH;
import static com.googlecode.gitst.RepoProperties.PROP_IGNORE;
import static com.googlecode.gitst.RepoProperties.PROP_THREADS;
import static com.googlecode.gitst.RepoProperties.PROP_URL;
import static com.googlecode.gitst.RepoProperties.PROP_USER_PATTERN;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Andrey Pavlenko
 */
public class Init {

    public static void main(final String[] args) {
        if (args.length == 0) {
            printHelp(System.out);
        } else {
            try {
                final Args a = new Args(args);
                final String host = a.get("-h");
                final int port = a.getInt("-p");
                final String project = a.get("-P");
                final String view = a.get("-V");
                final String branch = a.get("-b", "master");
                final String user = a.get("-u", null);
                final String password = a.get("-pwd", null);
                final String ca = a.get("-ca", null);
                final String ignore = a.get("-i", PROP_DEFAULT_IGNORE);
                final String up = a.get("-up", PROP_DEFAULT_USER_PATTERN);
                final String t = a.get("-t", PROP_DEFAULT_THREADS);
                final File dir = new File(a.get("-d", "."));
                final StringBuilder sb = new StringBuilder("st::starteam://");
                final String url;

                if (user != null) {
                    sb.append(user);

                    if (password != null) {
                        sb.append(':').append(password);
                    }

                    sb.append('@');
                }

                sb.append(host).append(':').append(port).append('/')
                        .append(project).append('/').append(view);
                url = sb.toString();

                if (!dir.exists()) {
                    dir.mkdirs();
                }

                final Git git = new Git(dir);

                if (a.hasOption("--bare")) {
                    git.exec("init", "--bare").exec().waitFor();
                } else {
                    git.exec("init").exec().waitFor();
                }

                final RepoProperties props = new RepoProperties(git, "origin");
                props.setLocalProperty(PROP_URL, url);
                props.setLocalProperty(PROP_FETCH, "+refs/heads/" + branch
                        + ":refs/remotes/origin/master");
                props.setLocalProperty(PROP_THREADS,
                        String.valueOf(Integer.parseInt(t)));
                props.setLocalProperty(PROP_USER_PATTERN, up);
                props.setLocalProperty(PROP_IGNORE, ignore);

                if (ca != null) {
                    props.setLocalProperty(PROP_CA, ca);
                }

                props.saveLocalProperties();
                git.exec("config", "core.ignorecase", "false").exec().waitFor();
            } catch (final IllegalArgumentException ex) {
                System.err.println(ex.getMessage());
                printHelp(System.err);
                System.exit(1);
            } catch (final IOException | InterruptedException ex) {
                System.err.println(ex.getMessage());
                System.exit(1);
            } catch (final ExecutionException ex) {
                System.err.println(ex.getMessage());
                System.exit(ex.getExitCode());
            }
        }
    }

    private static void printHelp(final PrintStream ps) {
        ps.println("Usage: git st init -h <host> -p <port> -P <project> -V <view> "
                + "[-u <user>] [-b <branch>] [-d <directory>] [-ca <CacheAgent>] "
                + "[-t <MaxThreads>] [-up <userpattern>] [-i <ignore>] [--bare]");
    }
}
