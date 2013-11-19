require 'rubygems/local_remote_options'
require 'net/http'
require 'base64'

class Gem::AbstractCommand < Gem::Command
  include Gem::LocalRemoteOptions

  def initialize( name, summary )
    super
   
    add_option('-c', '--nexus-clear',
               'Clears the nexus config') do |value, options|
      options[:nexus_clear] = value
    end
    add_option('--nexus-config FILE',
               'File location of nexus config') do |value, options|
      options[:nexus_config] = File.expand_path( value )
    end
    add_option('--repo KEY',
               'pick the config under that key') do |value, options|
      options[:nexus_repo] = value
    end
  end

  def url
    url = config[:url]
    # no leading slash
    url.sub!(/\/$/,'') if url
    url
  end

  def configure_url
    say "Enter the URL of the rubygems repository on a Nexus server"

    url = ask("URL: ")

    if URI.parse( "#{url}" ).host != nil
      store_config(:url, url)

      say 'The Nexus URL has been stored in ~/.gem/nexus'
    else
      raise 'no URL given'
    end
  end

  def setup
    configure_url if !config.key?( :url ) || options[:nexus_clear]
    use_proxy!( url ) if http_proxy( url )
    sign_in if !config.key?( :authorization ) || options[:nexus_clear]
  end

  def sign_in
    say "Enter your Nexus credentials"
    username = ask("Username: ")
    password = ask_for_password("Password: ")

    # mimic strict_encode64 which is not there on ruby1.8
    token = "#{username}:#{password}"
    if token != ':'
      store_config(:authorization, 
                   "Basic #{Base64.encode64(username + ':' + password).gsub(/\s+/, '')}")
    else
      store_config(:authorization, nil )
    end

    say "Your Nexus credentials has been stored in ~/.gem/nexus"
  end

  def config_path
    options[:nexus_config] || File.join( Gem.user_home, '.gem', 'nexus' )
  end

  def all_configs
    @all_configs ||= Gem.configuration.load_file(config_path)
  end
  private :all_configs

  def this_config
    if options[ :nexus_repo ]
      all_configs[ options[ :nexus_repo ] ] ||= {}
    else
      all_configs
    end
  end
  private :this_config
  
  def config
    @config ||= this_config
  end

  def authorization
    config[:authorization]
  end

  def store_config(key, value)
    this_config.merge!(key => value)
    dirname = File.dirname(config_path)
    Dir.mkdir(dirname) unless File.exists?(dirname)

    File.open(config_path, 'w') do |f|
      f.write all_configs.to_yaml
    end
  end

  def make_request(method, path)
    require 'net/http'
    require 'net/https'

    url = URI.parse( "#{self.url}/#{path}" )

    http = proxy_class.new( url.host, url.port )

    if url.scheme == 'https'
      http.use_ssl = true
    end
    
    #Because sometimes our gems are huge and our people are on vpns
    http.read_timeout = 300

    request_method =
      case method
      when :get
        proxy_class::Get
      when :post
        proxy_class::Post
      when :put
        proxy_class::Put
      when :delete
        proxy_class::Delete
      else
        raise ArgumentError
      end

    request = request_method.new( url.path )
    request.add_field "User-Agent", "Ruby" unless RUBY_VERSION =~ /^1.9/

    yield request if block_given?
    
    if Gem.configuration.verbose.to_s.to_i > 0
      warn "#{request.method} #{url.to_s}"
      if authorization
        warn 'use authorization' 
      else
        warn 'no authorization'
      end
 
      warn "use proxy at #{http.proxy_address}:#{http.proxy_port}" if http.proxy_address
    end

    http.request(request)
  end

  def use_proxy!( url )
    proxy_uri = http_proxy( url )
    @proxy_class = Net::HTTP::Proxy(proxy_uri.host, proxy_uri.port, proxy_uri.user, proxy_uri.password)
  end

  def proxy_class
    @proxy_class || Net::HTTP
  end

  # @return [URI, nil] the HTTP-proxy as a URI if set; +nil+ otherwise
  def http_proxy( url )
    uri = URI.parse( url ) rescue nil
    return nil if uri.nil?
    if no_proxy = ENV[ 'no_proxy' ] || ENV[ 'NO_PROXY' ]
      # does not look on ip-adress ranges
      return nil if no_proxy.split( /, */ ).member?( uri.host )
    end
    key = uri.scheme == 'http' ? 'http_proxy' : 'https_proxy'
    proxy = Gem.configuration[ :http_proxy ] || ENV[ key ] || ENV[ key.upcase ]
    return nil if proxy.nil? || proxy == :no_proxy

    URI.parse( proxy )
  end

  def ask_for_password(message)
    system "stty -echo"
    password = ask(message)
    system "stty echo"
    ui.say("\n")
    password
  end
end
