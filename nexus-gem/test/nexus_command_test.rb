require 'command_helper'

class NexusCommandTest < CommandTest
  context "pushing" do
    setup do
      @command = Gem::Commands::NexusCommand.new
      stub(@command).say
    end

    should "setup and send the gem" do
      mock(@command).setup
      mock(@command).send_gem
      @command.execute
      assert_received(@command) { |command| command.setup }
      assert_received(@command) { |command| command.send_gem }
    end

    should "raise an error with no arguments" do
      assert_raise Gem::CommandLineError do
        @command.send_gem
      end
    end

    context "pushing a gem" do
      setup do

        @gem_path = "path/to/foo-0.0.0.gem"
        baseurl = 'http://localhost:8081/nexus/content/repositories/localgems'
        @url = baseurl + @gem_path.sub(/.*\//, '/gems/')
        @gem_binary = StringIO.new("gem")

        stub(@command).say
        stub(@command).options { {:args => [@gem_path]} }
        stub(Gem).read_binary(@gem_path) { @gem_binary }
        stub(@command).config { { :authorization => "key", :url => baseurl } }
        stub_request(:post, @url).to_return(:status => 201)
        
        @command.send_gem
      end

      should "say push was successful" do
        assert_received(@command) { |command| command.say("Uploading gem to Nexus...") }
        # due to webmock there is no status message
        assert_received(@command) { |command| command.say("") }
      end

      should "post to api" do
        # webmock doesn't pass body params on correctly :[
        assert_requested(:post, @url,
                         :times => 1)
        assert_requested(:post, @url,
                         :body => @gem_binary,
                         :headers => {
                           'Authorization' => 'key', 
                           'Content-Type' => 'application/octet-stream', 
                           'User-Agent'=>'Ruby'
                         })
      end
    end
  end
end
