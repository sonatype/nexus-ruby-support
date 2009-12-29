require 'rubygems'
require 'rubygems/indexer'
require 'yaml'

class Gem::NexusIndexer < Gem::Indexer
  def collect_specs(yamls = gemspec_file_list)
    index = Gem::SourceIndex.new
    progress = ui.progress_reporter yamls.size, "Loading #{yamls.size} yamls", "loaded all yamls"
    Gem.time 'loaded' do 
      yamls.each do |yamlFile|
        if File.size(yamlFile.to_s) == 0 then
          alert_warning "Skipping zero-length yaml: #{yamlFile}"
	  next
        end

	begin
	  spec = nil
	  File.open( yamlFile ) do | yml |
	    spec = Gem::Specification.from_yaml(yml)
	  end
	  spec.loaded_from = yamlFile

          unless yamlFile =~ /\/#{Regexp.escape spec.original_name}.*\.gemspec\z/i then
            expected_name = spec.full_name
	    expected_name << " (#{spec.original_name})" if
	      spec.original_name != spec.full_name
	    alert_warning "Skipping misnamed gem: #{yamlFile} should be named #{expected_name}"
	    next
	  end

	  abbreviate spec
	  sanitize spec

	  index.add_spec spec, spec.original_name

	  progress.updated spec.original_name

	  rescue SignalException => e
	    alert_error "Received signal, existing"
	    raise
	  rescue Exception => e
	    alert_error "Unable to process #{yamlFile}\n#{e.message} (#{e.class})\n\t#{e.backtrace.join "\n\t"}"

	end
      end
      progress.done
    end
    index
  end
  def gemspec_file_list
    Dir.glob(File.join(@dest_directory, "gems", "*.gemspec")) 
  end
end
