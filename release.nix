{ nixpkgs, system,
  impureCache ? false }:

let
  pkgs = import nixpkgs { inherit system; };
in {
  build = import ./default.nix {
    inherit pkgs;
    inherit impureCache;
  };
}
