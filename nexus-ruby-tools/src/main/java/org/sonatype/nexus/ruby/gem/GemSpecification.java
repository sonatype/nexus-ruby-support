package org.sonatype.nexus.ruby.gem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Gem::Specification
 * 
 * @author cstamas
 */
public class GemSpecification
{
    private List<String> authors;

    @Deprecated
    private String autorequire;

    private String bindir;

    private List<String> cert_chain;

    private Date date;

    private String default_executable;

    private List<Object> dependencies;

    private String description;

    private String email;

    private List<String> executables;

    private List<String> extensions;

    private List<String> extra_rdoc_files;

    private List<String> files;

    private boolean has_rdoc;

    private String homepage;

    private String name;

    private String platform;

    private List<String> rdoc_options;

    private List<String> require_paths;

    private GemRequirement required_ruby_version;

    private GemRequirement required_rubygems_version;

    private List<String> requirements;

    private String rubyforge_project;

    private String rubygems_version;

    private String specification_version;

    private String summary;

    private List<String> test_files;

    private GemVersion version;

    private List<String> licenses;

    private String post_install_message;

    private String signing_key;

    public void setAuthor( String author )
    {
        getAuthors().add( author );
    }

    public List<String> getAuthors()
    {
        if ( authors == null )
        {
            authors = new ArrayList<String>();
        }

        return authors;
    }

    public void setAuthors( List<String> authors )
    {
        this.authors = authors;
    }

    @Deprecated
    public String getAutorequire()
    {
        return autorequire;
    }

    @Deprecated
    public void setAutorequire( String autorequire )
    {
        this.autorequire = autorequire;
    }

    public String getBindir()
    {
        return bindir;
    }

    public void setBindir( String bindir )
    {
        this.bindir = bindir;
    }

    public List<String> getCert_chain()
    {
        if ( cert_chain == null )
        {
            cert_chain = new ArrayList<String>();
        }

        return cert_chain;
    }

    public void setCert_chain( List<String> certChain )
    {
        cert_chain = certChain;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate( Date date )
    {
        this.date = date;
    }

    public String getDefault_executable()
    {
        return default_executable;
    }

    public void setDefault_executable( String defaultExecutable )
    {
        default_executable = defaultExecutable;
    }

    public List<Object> getDependencies()
    {
        if ( dependencies == null )
        {
            dependencies = new ArrayList<Object>();
        }

        return dependencies;
    }

    public void setDependencies( List<Object> dependencies )
    {
        getDependencies().addAll( dependencies );
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public List<String> getExecutables()
    {
        if ( executables == null )
        {
            executables = new ArrayList<String>();
        }

        return executables;
    }

    public void setExecutables( List<String> executables )
    {
        this.executables = executables;
    }

    public List<String> getExtensions()
    {
        if ( extensions == null )
        {
            extensions = new ArrayList<String>();
        }

        return extensions;
    }

    public void setExtensions( List<String> extensions )
    {
        this.extensions = extensions;
    }

    public List<String> getExtra_rdoc_files()
    {
        if ( extra_rdoc_files == null )
        {
            extra_rdoc_files = new ArrayList<String>();
        }

        return extra_rdoc_files;
    }

    public void setExtra_rdoc_files( List<String> extraRdocFiles )
    {
        extra_rdoc_files = extraRdocFiles;
    }

    public List<String> getFiles()
    {
        if ( files == null )
        {
            files = new ArrayList<String>();
        }

        return files;
    }

    public void setFiles( List<String> files )
    {
        this.files = files;
    }

    public boolean isHas_rdoc()
    {
        return has_rdoc;
    }

    public void setHas_rdoc( boolean hasRdoc )
    {
        has_rdoc = hasRdoc;
    }

    public String getHomepage()
    {
        return homepage;
    }

    public void setHomepage( String homepage )
    {
        this.homepage = homepage;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getPlatform()
    {
        return platform;
    }

    public void setPlatform( String platform )
    {
        this.platform = platform;
    }

    public List<String> getRdoc_options()
    {
        if ( rdoc_options == null )
        {
            rdoc_options = new ArrayList<String>();
        }

        return rdoc_options;
    }

    public void setRdoc_options( List<String> rdocOptions )
    {
        rdoc_options = rdocOptions;
    }

    public List<String> getRequire_paths()
    {
        if ( require_paths == null )
        {
            require_paths = new ArrayList<String>();
        }

        return require_paths;
    }

    public void setRequire_paths( List<String> requirePaths )
    {
        require_paths = requirePaths;
    }

    public GemRequirement getRequired_ruby_version()
    {
        return required_ruby_version;
    }

    public void setRequired_ruby_version( GemRequirement requiredRubyVersion )
    {
        required_ruby_version = requiredRubyVersion;
    }

    public GemRequirement getRequired_rubygems_version()
    {
        return required_rubygems_version;
    }

    public void setRequired_rubygems_version( GemRequirement requiredRubygemsVersion )
    {
        required_rubygems_version = requiredRubygemsVersion;
    }

    public List<String> getRequirements()
    {
        if ( requirements == null )
        {
            requirements = new ArrayList<String>();
        }

        return requirements;
    }

    public void setRequirements( List<String> requirements )
    {
        this.requirements = requirements;
    }

    public String getRubyforge_project()
    {
        return rubyforge_project;
    }

    public void setRubyforge_project( String rubyforgeProject )
    {
        rubyforge_project = rubyforgeProject;
    }

    public String getRubygems_version()
    {
        return rubygems_version;
    }

    public void setRubygems_version( String rubygemsVersion )
    {
        rubygems_version = rubygemsVersion;
    }

    public String getSpecification_version()
    {
        return specification_version;
    }

    public void setSpecification_version( String specificationVersion )
    {
        specification_version = specificationVersion;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary( String summary )
    {
        this.summary = summary;
    }

    public void setTest_file( String testFile )
    {
        getTest_files().add( testFile );
    }

    public List<String> getTest_files()
    {
        if ( test_files == null )
        {
            test_files = new ArrayList<String>();
        }

        return test_files;
    }

    public void setTest_files( List<String> testFiles )
    {
        test_files = testFiles;
    }

    public GemVersion getVersion()
    {
        return version;
    }

    public void setVersion( GemVersion version )
    {
        this.version = version;
    }

    public List<String> getLicenses()
    {
        if ( licenses == null )
        {
            licenses = new ArrayList<String>();
        }

        return licenses;
    }

    public void setLicenses( List<String> licenses )
    {
        this.licenses = licenses;
    }

    public String getPost_install_message()
    {
        return post_install_message;
    }

    public void setPost_install_message( String postInstallMessage )
    {
        post_install_message = postInstallMessage;
    }

    public String getSigning_key()
    {
        return signing_key;
    }

    public void setSigning_key( String signingKey )
    {
        signing_key = signingKey;
    }

}
