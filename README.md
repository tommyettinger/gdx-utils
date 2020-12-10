## gdx-utils
An updated fork of dermetfan's famed library.

BitBucket has deleted their Mercurial repositories, including gdx-utils, so a backup is needed.
Also, libgdx-utils was stuck on version 1.9.6 of libGDX, and now we can update to 1.9.12, plus a Gradle update.
LibGDX is on 1.9.12 now and a new version is in the works, but libgdx-utils should be compatible with those versions
thanks to Gradle, without needing a new release here. There were some backwards-incompatible changes in 1.9.12 relative
to 1.9.11, but now gdx-utils is compatible with 1.9.12 thanks to @barkholt . Updating is encouraged; you can use JitPack,
or you can use the standard Sonatype Maven Central repository for at least version 0.13.7. Dependency for Gradle:

```groovy
api "com.github.tommyettinger:libgdx-utils:0.13.7"
```

- [libgdx-utils API Documentation](https://tommyettinger.github.io/gdx-utils/libgdx-utils/apidocs/index.html)
- [libgdx-utils-box2d API Documentation](https://tommyettinger.github.io/gdx-utils/libgdx-utils-box2d/apidocs/index.html)
- Build Status: just check [JitPack](https://jitpack.io/#tommyettinger/gdx-utils); tell it to build any release or commit you want.
- [Wiki](https://man.sr.ht/~dermetfan/libgdx-utils/)
  - The wiki is back up again on SourceHut!
- [~~Homepage~~](http://dermetfan.net/libgdx-utils.php)
  - The homepage is down; dermetfan is doing something different. The wiki should be most of what matters.

# Philosophy #

~~Legacy tends to prevent you from writing the best code possible. Being compatible with previous versions is therefore
of low priority.~~ ...lol, that's a really awful philosophy. Working code is better than what was at one point the "best
code possible." If you're using this library, be aware:  you are using legacy code, and people have been using this code
for a long time now.
