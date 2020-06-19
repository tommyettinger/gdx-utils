{ pkgs ? import <nixpkgs> {},
  impureCache ? false }:

pkgs.stdenv.mkDerivation {
  name = "libgdx-utils";
  src = ./.;

  buildInputs = with pkgs; [
    openjdk8
  ];

  buildPhase = ''
    export GRADLE_USER_HOME=${if impureCache then "/tmp/.gradle" else ".gradle"}
    ./gradlew build --no-daemon
  '';

  installPhase = ''
    install_project() {
      local build=$1
      local name=$2
    
      mkdir -p $out/nix-support
    
      local doc=$out/share/doc/$name
      mkdir -p $doc
      mv $build/docs/javadoc/ $doc
      mv $build/libs/$name-*-javadoc.jar $doc
      echo file doc-dist $doc/$name-*-javadoc.jar >> $out/nix-support/hydra-build-products

      local src=$out/src
      mkdir -p $src
      mv $build/libs/$name-*-sources.jar $src
      echo file source-dist $src/$name-*-sources.jar >> $out/nix-support/hydra-build-products

      local lib=$out/lib
      mkdir -p $lib
      mv $build/libs/$name-*.jar $lib
      echo file binary-dist $lib/$name-*.jar >> $out/nix-support/hydra-build-products
    }

    install_project build libgdx-utils
    install_project box2d/build libgdx-utils-box2d
  '';

  meta = with pkgs.lib; {
    license = licenses.asl20;
    homepage = https://github.com/tommyettinger/gdx-utils;
    maintainers = [ "serverkorken@gmail.com" ];
  };
}
