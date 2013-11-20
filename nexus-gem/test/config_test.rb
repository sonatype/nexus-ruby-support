require 'minitest/autorun'
require 'shoulda'
require 'fileutils'

class ConfigTest < ::MiniTest::Unit::TestCase
  include ShouldaContextLoadable 
  
  context 'file' do

    should 'store key/values with repo' do
      file = File.join( 'pkg', 'cfg' )
      FileUtils.rm_f file
      f = Nexus::Config::File.new( file, 'first' )
      f[ 'asd' ] = 'dsa'
      assert_equal( f.store, true )
      ff = Nexus::Config::File.new( file, 'first' )
      assert_equal( ff[ 'asd' ], 'dsa' )
      ff = Nexus::Config::File.new( file, nil )
      assert_equal( ff[ 'asd' ], nil )
    end

    should 'store key/values without repo' do
      file = File.join( 'pkg', 'cfg' )
      FileUtils.rm_f file
      f = Nexus::Config::File.new( file, nil )
      f[ 'asd' ] = 'dsa'
      assert_equal( f.store, true )
      ff = Nexus::Config::File.new( file, nil )
      assert_equal( ff[ 'asd' ], 'dsa' )
      ff = Nexus::Config::File.new( file, 'first' )
      assert_equal( ff[ 'asd' ], nil )
    end

    should 'not store anything' do
      f = Nexus::Config::File.new( nil, nil )
      f[ 'asd' ] = 'dsa'
      assert_equal( f.store, false )
      ff = Nexus::Config::File.new( nil, 'first' )
      f[ 'asd' ] = 'dsa'
      assert_equal( f.store, false )
    end
  end

  context 'config' do

    should 'not use secrets file' do
      file = File.join( 'pkg', 'cfg' )
      FileUtils.rm_f file
      c = Nexus::Config.new( :second, file, nil )
      c[ 'asd' ] = 'dsa'
      assert_equal c.key?( 'asd' ), true
      
      cc = Nexus::Config::File.new( file, :second )
      assert_equal cc.key?( 'asd' ), true
      assert_equal cc[ 'asd' ], 'dsa'
    
      cc = Nexus::Config::File.new( file, nil )
      assert_equal cc.key?( 'asd' ), false
      assert_equal cc[ 'asd' ], nil

      cc = Nexus::Config::File.new( file, 'third' )
      assert_equal cc.key?( 'asd' ), false
      assert_equal cc[ 'asd' ], nil

      cc = Nexus::Config.new( :second, file, nil )
      assert_equal cc.key?( 'asd' ), true
      assert_equal cc[ 'asd' ], 'dsa'

      cc = Nexus::Config.new( nil, file, nil )
      assert_equal cc.key?( 'asd' ), false
      assert_equal cc[ 'asd' ], nil

      cc = Nexus::Config.new( 'third', file, nil )
      assert_equal cc.key?( 'asd' ), false
      assert_equal cc[ 'asd' ], nil
    end

    should 'use secrets file' do
      file = File.join( 'pkg', 'cfgstub' )
      sfile = File.join( 'pkg', 'cfgsecrets' )
      FileUtils.rm_f file
      FileUtils.rm_f sfile
      c = Nexus::Config.new( :second, file, sfile )
      c[ 'asd' ] = 'dsa'
      c[ :authorization ] = 'BASIC asddsa'

      assert_equal c.key?( :secrets ), false
      assert_equal c.key?( 'asd' ), true
      assert_equal c.key?( :authorization ), true
      
      cc = Nexus::Config::File.new( file, :second )
      assert_equal cc.key?( :secrets ), false
      assert_equal cc.key?( :authorization ), false
      assert_equal cc.key?( 'asd' ), true
      assert_equal cc[ 'asd' ], 'dsa'
    
      cc = Nexus::Config::File.new( file, nil )
      assert_equal cc.key?( :secrets ), true
      assert_equal cc.key?( :authorization ), false
      assert_equal cc.key?( 'asd' ), false
      assert_equal cc[ :secrets ], sfile

      cc = Nexus::Config::File.new( file, 'third' )
      assert_equal cc.key?( :secrets ), false
      assert_equal cc.key?( :authorization ), false
      assert_equal cc.key?( 'asd' ), false

      cc = Nexus::Config.new( :second, file, nil )
      assert_equal cc.key?( :secrets ), false
      assert_equal cc.key?( :authorization ), true
      assert_equal cc.key?( 'asd' ), true
      assert_equal cc[ 'asd' ], 'dsa'
      assert_equal cc[ :authorization ], 'BASIC asddsa'

      cc = Nexus::Config.new( nil, file, nil )
      assert_equal cc.key?( :secrets ), true
      assert_equal cc.key?( :authorization ), false
      assert_equal cc.key?( 'asd' ), false
      assert_equal cc[ :secrets ], sfile

      cc = Nexus::Config.new( 'third', file, nil )
      assert_equal cc.key?( :secrets ), false
      assert_equal cc.key?( :authorization ), false
      assert_equal cc.key?( 'asd' ), false
    end

    should 'copy authorization when starting to use secrets file' do
      file = File.join( 'pkg', 'cfgstub' )
      sfile = File.join( 'pkg', 'cfgsecrets' )
      FileUtils.rm_f file
      FileUtils.rm_f sfile
      c = Nexus::Config.new( nil, file, nil )
      c[ 'asd' ] = 'dsa'
      c[ :authorization ] = 'BASIC asddsa'

      assert_equal c.key?( 'asd' ), true
      assert_equal c.key?( :authorization ), true
      
      c = Nexus::Config.new( nil, file, sfile )
      assert_equal c.key?( :secrets ), true
      assert_equal c.key?( 'asd' ), true
      assert_equal c.key?( :authorization ), true
      assert_equal c[ :authorization ], 'BASIC asddsa'
      assert_equal c[ :secrets ], sfile
      
      cc = Nexus::Config::File.new( sfile, nil )
      assert_equal cc.key?( :secrets ), false
      assert_equal cc.key?( :authorization ), true
      assert_equal cc.key?( 'asd' ), false
      assert_equal cc[ :authorization ], 'BASIC asddsa'

      cc = Nexus::Config::File.new( file, nil )
      assert_equal cc.key?( :secrets ), true
      assert_equal cc.key?( :authorization ), false
      assert_equal cc.key?( 'asd' ), true
      assert_equal cc[ :secrets ], sfile

      cc = Nexus::Config::File.new( file, 'third' )
      assert_equal cc.key?( :secrets ), false
      assert_equal cc.key?( :authorization ), false
      assert_equal cc.key?( 'asd' ), false
    end
  end
end
