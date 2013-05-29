require 'command_helper'

class Gem::Commands::FakeCommand < Gem::AbstractCommand
  def description
    'fake command'
  end

  def initialize
    super 'fake', description
  end

  def execute
  end
end

class AbstractCommandTest < CommandTest
  context "with an fake command" do
    setup do
      @command = Gem::Commands::FakeCommand.new
      stub(@command).say
      ENV['http_proxy'] = nil
      ENV['HTTP_PROXY'] = nil
    end

    context "parsing the proxy" do
      should "return nil if no proxy is set" do
        stub_config(:http_proxy => nil)
        assert_equal nil, @command.http_proxy
      end

      should "return nil if the proxy is set to :no_proxy" do
        stub_config(:http_proxy => :no_proxy)
        assert_equal nil, @command.http_proxy
      end

      should "return a proxy as a URI if set" do
        stub_config( :http_proxy => 'http://proxy.example.org:9192' )
        assert_equal 'proxy.example.org', @command.http_proxy.host
        assert_equal 9192, @command.http_proxy.port
      end

      should "return a proxy as a URI if set by environment variable" do
        ENV['http_proxy'] = "http://jack:duck@192.168.1.100:9092"
        assert_equal "192.168.1.100", @command.http_proxy.host
        assert_equal 9092, @command.http_proxy.port
        assert_equal "jack", @command.http_proxy.user
        assert_equal "duck", @command.http_proxy.password
      end
    end

    should "sign in if authorization and no nexus url" do
      stub(@command).authorization { nil }
      stub(@command).url { nil }
      stub(@command).sign_in
      stub(@command).configure_url
      @command.setup
      assert_received(@command) { |command| command.configure_url }
      assert_received(@command) { |command| command.sign_in }
    end

    should "sign in if --clear-config is set" do
      stub(@command).sign_in
      stub(@command).configure_url
      stub(@command).options { {:nexus_clear => true} }
      @command.setup
      assert_received(@command) { |command| command.sign_in }
      assert_received(@command) { |command| command.configure_url }
    end

    should "not sign in nor configure if authorizaton and url exists" do
      stub(@command).authorization { "1234567890" }
      stub(@command).url { "abc" }
      stub(@command).sign_in
      stub(@command).configure_url
      @command.setup
      assert_received(@command) { |command| command.configure_url.never }
      assert_received(@command) { |command| command.sign_in.never }
    end

    context "using the proxy" do
      setup do
        stub_config( :http_proxy => "http://gilbert:sekret@proxy.example.org:8081" )
        @proxy_class = Object.new
        mock(Net::HTTP).Proxy('proxy.example.org', 8081, 'gilbert', 'sekret') { @proxy_class }
        @command.use_proxy!
      end

      should "replace Net::HTTP with a proxy version" do
        assert_equal @proxy_class, @command.proxy_class
      end
    end

    context "signing in" do
      setup do
        @username = "username"
        @password = "password 01234567890123456789012345678901234567890123456789"
        @key = "key"

        stub(@command).say
        stub(@command).ask { @username }
        stub(@command).ask_for_password { @password }
        stub(@command).store_config { {:authorization => @key} }
      end

      should "ask for username and password" do
        @command.sign_in
        assert_received(@command) { |command| command.ask("Username: ") }
        assert_received(@command) { |command| command.ask_for_password("Password: ") }
        assert_received(@command) { |command| command.store_config(:authorization, "Basic dXNlcm5hbWU6cGFzc3dvcmQgMDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODk=") }
      end

      should "say that we signed in" do
        @command.sign_in
        assert_received(@command) { |command| command.say("Enter your Nexus credentials") }
        assert_received(@command) { |command| command.say("Your Nexus credentials has been stored in ~/.gem/nexus") }
        assert_received(@command) { |command| command.store_config(:authorization, "Basic dXNlcm5hbWU6cGFzc3dvcmQgMDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODk=") }
      end
    end

    context "configure nexus url" do
      setup do
        @url = "url"

        stub(@command).say
        stub(@command).ask { @url }
        stub(@command).store_config { {:url => @url} }
      end

      should "ask for nexus url" do
        @command.configure_url
        assert_received(@command) { |command| command.ask("URL: ") }
        assert_received(@command) { |command| command.store_config(:url, "url") }
      end

      should "say that we configured the url" do
        @command.configure_url
        assert_received(@command) { |command| command.say("Enter the URL of the rubygems repository on a Nexus server") }
        assert_received(@command) { |command| command.say("The Nexus URL has been stored in ~/.gem/nexus") }
        assert_received(@command) { |command| command.store_config(:url, "url") }
      end
    end
  end
end
