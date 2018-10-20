package com.github.wwadge.hbnpojogen;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;


/**
 * The Hibernate Sychronizer mojo.
 *
 * @author wallacew
 * @goal generate
 * @phase generate-sources
 * @description Creates all the hibernate/spring stuff required to access a DB
 */
public class SynchronizerMojo extends AbstractMojo {

    /**
     * The name of the XML config file to use. If you omit this, attempt to load hbnpojogen.config.xml
     * from the classpath.
     *
     * @parameter property="configFile"
     * @optional
     */
    protected String configFile;

    /**
     * The artifactId of the child that will contain the generated sources.
     *
     * @parameter property="modelArtifactId"
     * @required
     */
    protected String modelArtifactId;

    /**
     * The project object model.
     *
     * @parameter property="project"
     * @readonly
     * @required
     */
    protected MavenProject mavenProject;

    /**
     * ipaddress to use as source (override).
     *
     * @parameter property="ip"
     * @optional
     */
    protected String ip;

    /**
     * temp.
     */
    protected String target;

    /**
     * If this is declared, the model generation will start.
     *
     * @parameter property="generateModel"
     * @optional
     */
    protected Boolean generateModel;

    // @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.generateModel == null || !this.generateModel) {
            getLog().info("Skipping model generation. Run with -DgenerateModel to enable.");
        } else {

            File f = new File(this.mavenProject.getBasedir(), this.configFile == null ? "hbnpojogen.config.xml" : this.configFile);
            if (f.exists()) {
                this.configFile = f.getAbsolutePath();
            } else {
                throw new MojoFailureException("Could not find " + f.getAbsolutePath());
            }

            this.target = this.mavenProject.getBasedir().getAbsolutePath() + File.separator + this.modelArtifactId;
            HbnPojoGen.setLog(getLog());
            HbnPojoGen.run(this.configFile, this.target, this.ip);

        }
    }


}
