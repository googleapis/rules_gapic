# Changelog

## [0.14.0](https://github.com/googleapis/rules_gapic/compare/v0.13.0...v0.14.0) (2022-06-17)


### Features

* Make `transport = "grpc+rest"` default for java and golang ([#103](https://github.com/googleapis/rules_gapic/issues/103)) ([993109d](https://github.com/googleapis/rules_gapic/commit/993109df0d17cd51164819f4ae5cdc80d335ef74))

## [0.13.0](https://github.com/googleapis/rules_gapic/compare/v0.12.1...v0.13.0) (2022-06-09)


### Features

* **bazel:** add include_samples to java_gapic_assembly_gradle_pkg ([d1bc568](https://github.com/googleapis/rules_gapic/commit/d1bc56854536541134b24ca6ce5aff8cfdd45937))
* Treat `transport` parameter as preserved (not overriden by default) parameter ([#100](https://github.com/googleapis/rules_gapic/issues/100)) ([e3a1f12](https://github.com/googleapis/rules_gapic/commit/e3a1f12a61e61e5223f910b31e5c4d1729cd9d4b))


### Bug Fixes

* **bazel:** add service_yaml to ruby_cloud_gapic_library ([#99](https://github.com/googleapis/rules_gapic/issues/99)) ([863addb](https://github.com/googleapis/rules_gapic/commit/863addbffb78ef5998e7c24620273bddbad80bb0))

### [0.12.1](https://github.com/googleapis/rules_gapic/compare/v0.12.0...v0.12.1) (2022-04-14)


### Bug Fixes

* **bazel:** add service_yaml to py_gapic_library ([#97](https://github.com/googleapis/rules_gapic/issues/97)) ([39fad2d](https://github.com/googleapis/rules_gapic/commit/39fad2d73cf6163e38015a8ad9fa29244f8eb6ba))

## [0.12.0](https://github.com/googleapis/rules_gapic/compare/v0.11.1...v0.12.0) (2022-02-15)


### Features

* Include service_yaml for C# ([1f15280](https://github.com/googleapis/rules_gapic/commit/1f15280c2b5e565ed2c73e90239b40d5ad454783))

### [0.11.1](https://www.github.com/googleapis/rules_gapic/compare/v0.11.0...v0.11.1) (2021-11-17)


### Bug Fixes

* missing flag in cc_grpc_library ([#90](https://www.github.com/googleapis/rules_gapic/issues/90)) ([940ffd3](https://www.github.com/googleapis/rules_gapic/commit/940ffd32f7a0fc992e44905e88ded674bd777122))

## [0.11.0](https://www.github.com/googleapis/rules_gapic/compare/v0.10.0...v0.11.0) (2021-11-16)


### Features

* generate C++ rules ([#87](https://www.github.com/googleapis/rules_gapic/issues/87)) ([e23c181](https://www.github.com/googleapis/rules_gapic/commit/e23c18143940899e71d23630f303a9325f441767))


### Bug Fixes

* **bazel:** iam/location mixin java deps ([#89](https://www.github.com/googleapis/rules_gapic/issues/89)) ([965fe25](https://www.github.com/googleapis/rules_gapic/commit/965fe2530a2340fe262982ebc2f8d6a0a68dbca0))

## [0.10.0](https://www.github.com/googleapis/rules_gapic/compare/v0.9.1...v0.10.0) (2021-11-02)


### Features

* add new C# parameter to the exception list ([#85](https://www.github.com/googleapis/rules_gapic/issues/85)) ([4859a1e](https://www.github.com/googleapis/rules_gapic/commit/4859a1ee806e44fca7bd08d61562d2826171f3bc))
* remove old ruby and Csharp rules ([#84](https://www.github.com/googleapis/rules_gapic/issues/84)) ([f2e590d](https://www.github.com/googleapis/rules_gapic/commit/f2e590dff5a7f6bf9dcf0c35c55530bed978afa2))

### [0.9.1](https://www.github.com/googleapis/rules_gapic/compare/v0.9.0...v0.9.1) (2021-10-28)


### Bug Fixes

* Preserve all manual changes to API_ROOT files for now ([#82](https://www.github.com/googleapis/rules_gapic/issues/82)) ([80fdf50](https://www.github.com/googleapis/rules_gapic/commit/80fdf5070405dcb466952cd7d0d13c15cc3ce147))

## [0.9.0](https://www.github.com/googleapis/rules_gapic/compare/v0.8.0...v0.9.0) (2021-09-29)


### Features

* **bazel:** map operation_metadata.proto to common_proto ([#77](https://www.github.com/googleapis/rules_gapic/issues/77)) ([f078303](https://www.github.com/googleapis/rules_gapic/commit/f0783031360390d27fbf7f02c854af74b0e31d48))

## [0.8.0](https://www.github.com/googleapis/rules_gapic/compare/v0.7.0...v0.8.0) (2021-09-29)


### Features

* make C# generated proto files end with .g.cs ([#76](https://www.github.com/googleapis/rules_gapic/issues/76)) ([7698d5c](https://www.github.com/googleapis/rules_gapic/commit/7698d5cf6489d385b055869e2721bac54b9d5114))

## [0.7.0](https://www.github.com/googleapis/rules_gapic/compare/v0.6.3...v0.7.0) (2021-09-20)


### Features

* make C# grpc files end with Grpc.g.cs ([#74](https://www.github.com/googleapis/rules_gapic/issues/74)) ([c9dc26c](https://www.github.com/googleapis/rules_gapic/commit/c9dc26c8390523522708885533fb620b376ba03b))

### [0.6.3](https://www.github.com/googleapis/rules_gapic/compare/v0.6.2...v0.6.3) (2021-09-14)


### Bug Fixes

* **bazel:** include iam Go dep for IAMPolicy mixin ([#71](https://www.github.com/googleapis/rules_gapic/issues/71)) ([55e3a6c](https://www.github.com/googleapis/rules_gapic/commit/55e3a6c875d3d211e2aef070d8c29c7c51bdb355))

### [0.6.2](https://www.github.com/googleapis/rules_gapic/compare/v0.6.1...v0.6.2) (2021-08-30)


### Bug Fixes

* correct overzealous locations search string ([#68](https://www.github.com/googleapis/rules_gapic/issues/68)) ([80272bb](https://www.github.com/googleapis/rules_gapic/commit/80272bb7f61589804236bbfa624929ce7db8dd62))

### [0.6.1](https://www.github.com/googleapis/rules_gapic/compare/v0.6.0...v0.6.1) (2021-08-25)


### Bug Fixes

* exclude lcoation mixin from proto input ([#66](https://www.github.com/googleapis/rules_gapic/issues/66)) ([6f9b0c7](https://www.github.com/googleapis/rules_gapic/commit/6f9b0c74e0c14eda1de8e17f21d9d3a746ec41e6))

## [0.6.0](https://www.github.com/googleapis/rules_gapic/compare/v0.5.5...v0.6.0) (2021-08-19)


### Features

* **build_gen:** add service_yaml to java_gapic_library ([#45](https://www.github.com/googleapis/rules_gapic/issues/45)) ([7db0098](https://www.github.com/googleapis/rules_gapic/commit/7db0098351882d1e06b8a4842f361b5be15a59bb))
* **build_gen:** inject Locations mixin proto deps ([#52](https://www.github.com/googleapis/rules_gapic/issues/52)) ([15c1f88](https://www.github.com/googleapis/rules_gapic/commit/15c1f88c64901bfa67a908efeaa4de850a8e8c7a))


### Bug Fixes

* **bazel:** include gapic_yaml in java if present ([#65](https://www.github.com/googleapis/rules_gapic/issues/65)) ([2b6fc8e](https://www.github.com/googleapis/rules_gapic/commit/2b6fc8e06faf86070683bcce4db241060dba8f73))
* **bazel:** retain certain google/api deps ([#63](https://www.github.com/googleapis/rules_gapic/issues/63)) ([455392f](https://www.github.com/googleapis/rules_gapic/commit/455392f678df6097b7a7089b0932475eba3a5a70)), closes [#62](https://www.github.com/googleapis/rules_gapic/issues/62)
* **bazel:** use None when no service_yaml ([#54](https://www.github.com/googleapis/rules_gapic/issues/54)) ([7bdaf87](https://www.github.com/googleapis/rules_gapic/commit/7bdaf871ef364818fa02247e1a001891d3f43091))

### [0.5.5](https://www.github.com/googleapis/rules_gapic/compare/v0.5.3...v0.5.5) (2021-06-16)


### Bug Fixes

* remove gen of protoc-gen-docs-plugin ([#47](https://www.github.com/googleapis/rules_gapic/issues/47)) ([c12c6dc](https://www.github.com/googleapis/rules_gapic/commit/c12c6dc44ce4de371f66dd450b3ac4f333c63f4b))


### Miscellaneous Chores

* release 0.5.5 ([#49](https://www.github.com/googleapis/rules_gapic/issues/49)) ([ae1fb5a](https://www.github.com/googleapis/rules_gapic/commit/ae1fb5a9abda1279aa3606d525615dfeae936692))

### [0.5.3](https://www.github.com/googleapis/rules_gapic/compare/v0.5.2...v0.5.3) (2021-04-05)


### Bug Fixes

* **build_gen:** handle errdetails_go_proto as a dep ([#40](https://www.github.com/googleapis/rules_gapic/issues/40)) ([1f0a0ef](https://www.github.com/googleapis/rules_gapic/commit/1f0a0ef3d5ae42cd816fdea043e4dc7cd9c47b3d))

### [0.5.2](https://www.github.com/googleapis/rules_gapic/compare/v0.5.1...v0.5.2) (2021-04-01)


### Bug Fixes

* **gapic:** generate gapic build on seeing service.yaml or grpc_service_config.json ([#37](https://www.github.com/googleapis/rules_gapic/issues/37)) ([1a62038](https://www.github.com/googleapis/rules_gapic/commit/1a62038b41919bdf34d3a3b83e22a0e73de3d3f9))
* **java:** remove IAM special-casing in test_classes generation ([#34](https://www.github.com/googleapis/rules_gapic/issues/34)) ([bebd02b](https://www.github.com/googleapis/rules_gapic/commit/bebd02bcb94758fe774b7e40495924d23a8f1f0c))

### [0.5.1](https://www.github.com/googleapis/rules_gapic/compare/v0.5.0...v0.5.1) (2021-03-12)


### Bug Fixes

* add options_proto to iam pkg mapping ([#31](https://www.github.com/googleapis/rules_gapic/issues/31)) ([cc63d3e](https://www.github.com/googleapis/rules_gapic/commit/cc63d3ee357550ccf1ba7f1459706b4ba081b8f9))

## [0.5.0](https://www.github.com/googleapis/rules_gapic/compare/v0.4.0...v0.5.0) (2021-02-24)


### Features

* add synth rules ([#6](https://www.github.com/googleapis/rules_gapic/issues/6)) ([108ecc0](https://www.github.com/googleapis/rules_gapic/commit/108ecc0aed3a91241ca114e16c9f1cd97dcf2d31))


### Bug Fixes

* properly support spaces in string values ([#27](https://www.github.com/googleapis/rules_gapic/issues/27)) ([ff1d244](https://www.github.com/googleapis/rules_gapic/commit/ff1d244dc6a8f531c8699f998de64ba82b2e2a71))

## [0.4.0](https://www.github.com/googleapis/rules_gapic/compare/v0.3.0...v0.4.0) (2021-02-19)


### Features

* add default value for the ruby gem name ([#22](https://www.github.com/googleapis/rules_gapic/issues/22)) ([428c05b](https://www.github.com/googleapis/rules_gapic/commit/428c05b06e0fe487f377929776285c81aa12e8c2))


### Bug Fixes

* **build_gen:** support proto java_package, make gapic.yaml dep optional, add tests [rules_gapic] ([#26](https://www.github.com/googleapis/rules_gapic/issues/26)) ([dc4dd84](https://www.github.com/googleapis/rules_gapic/commit/dc4dd840853ad7a929499f9b65c7ad66bb002dd4))
* ruby rules to have correct `srcs` form ([#20](https://www.github.com/googleapis/rules_gapic/issues/20)) ([475b182](https://www.github.com/googleapis/rules_gapic/commit/475b182282d51ccb58871a5ca2d3e10568baa7a1))

## [0.3.0](https://www.github.com/googleapis/rules_gapic/compare/v0.2.0...v0.3.0) (2021-02-17)


### Features

* **bazel:** enable Go metadata gen ([#16](https://www.github.com/googleapis/rules_gapic/issues/16)) ([748854c](https://www.github.com/googleapis/rules_gapic/commit/748854c6c95ed68eef741a6be6c6cc27ab86a05b))


### Bug Fixes

* renames and docs cleanup ([#18](https://www.github.com/googleapis/rules_gapic/issues/18)) ([7840242](https://www.github.com/googleapis/rules_gapic/commit/7840242217e08f51c75100d363b7e26667c8fa1d))

## [0.2.0](https://www.github.com/googleapis/rules_gapic/compare/v0.1.0...v0.2.0) (2021-02-12)


### Features

* add reserved ruby parameters ([#8](https://www.github.com/googleapis/rules_gapic/issues/8)) ([5b88dd7](https://www.github.com/googleapis/rules_gapic/commit/5b88dd7b2a5ee55b56fc72acf20a1b8aa23178cb))


### Bug Fixes

* change WORKSPACE name to rules_gapic ([#13](https://www.github.com/googleapis/rules_gapic/issues/13)) ([a6ba3b4](https://www.github.com/googleapis/rules_gapic/commit/a6ba3b48ea5c589c470db01c19b81e7208a1f27b))
* no need to have Node.js rules ([#11](https://www.github.com/googleapis/rules_gapic/issues/11)) ([73d2207](https://www.github.com/googleapis/rules_gapic/commit/73d22071f8af736859ae8287cd7b81ee198c02b9))
* remove monolith Go rules ([#4](https://www.github.com/googleapis/rules_gapic/issues/4)) ([fddc5df](https://www.github.com/googleapis/rules_gapic/commit/fddc5dfdf25374bee506ac0065afffe9856f19e2))
* update ruby rules to regenerate into ruby_cloud_gapic_library ([#7](https://www.github.com/googleapis/rules_gapic/issues/7)) ([0fe0094](https://www.github.com/googleapis/rules_gapic/commit/0fe0094de9ad210f0749d1273c3babbeac432e97))

## 0.1.0 (2021-02-08)


### ⚠ BREAKING CHANGES

* prepare for releasing into a separate repository

### Features

* add DIREGAPIC support for PHP ([#3305](https://www.github.com/googleapis/rules_gapic/issues/3305)) ([c38caeb](https://www.github.com/googleapis/rules_gapic/commit/c38caeb01c3259f90f051d539f09496db86837fd))
* Add opt_args to common_proto_library. ([#3191](https://www.github.com/googleapis/rules_gapic/issues/3191)) ([808b5e2](https://www.github.com/googleapis/rules_gapic/commit/808b5e2e85146ddf63fd38cce7131a4e455944f5))
* add opt_file_args to pass the file arguments to the protoc plugin ([#3239](https://www.github.com/googleapis/rules_gapic/issues/3239)) ([85a42fe](https://www.github.com/googleapis/rules_gapic/commit/85a42fe7b85aa6acd8c5458abf5d212d05bb59d6))
* add proto3_optional to proto_custom_library ([#3222](https://www.github.com/googleapis/rules_gapic/issues/3222)) ([2f88ad4](https://www.github.com/googleapis/rules_gapic/commit/2f88ad4fba77ef4189d07fe3e40f8e08b667954d))
* allow static substitution for group name ([#3146](https://www.github.com/googleapis/rules_gapic/issues/3146)) ([f16f63e](https://www.github.com/googleapis/rules_gapic/commit/f16f63e1f8a7ba181638d089510b9b50602f013f))
* build_gen all langs use grpc_service_config ([#3226](https://www.github.com/googleapis/rules_gapic/issues/3226)) ([3af766f](https://www.github.com/googleapis/rules_gapic/commit/3af766f7186d97e620d24a049438821f66cec9c7))
* generate GAPIC metadata file for Node.js by default ([#3313](https://www.github.com/googleapis/rules_gapic/issues/3313)) ([4e92a9b](https://www.github.com/googleapis/rules_gapic/commit/4e92a9b89d7958bd05384545cd93eb57240906e2))
* pre-release ([#2](https://www.github.com/googleapis/rules_gapic/issues/2)) ([96aa0b0](https://www.github.com/googleapis/rules_gapic/commit/96aa0b04066ea425d5257b48ded15502be8e11b4))
* prepare for releasing into a separate repository ([e8e91d6](https://www.github.com/googleapis/rules_gapic/commit/e8e91d679316c2118aa522ad5d5e3b7e9b410950))
* preserve some values when regenerating BUILD.bazel ([#3237](https://www.github.com/googleapis/rules_gapic/issues/3237)) ([880de2e](https://www.github.com/googleapis/rules_gapic/commit/880de2ec92c98d315efc1293d01c20e2f18af780))
* read version from gradle.properties if specified ([#3159](https://www.github.com/googleapis/rules_gapic/issues/3159)) ([23856a0](https://www.github.com/googleapis/rules_gapic/commit/23856a0a30c85ff79b390b4d16823db1cced75da))
* REST GAPIC (REGAPIC) Support for Java ([#3275](https://www.github.com/googleapis/rules_gapic/issues/3275)) ([027984c](https://www.github.com/googleapis/rules_gapic/commit/027984c5daff5d6e85390dd81c75e3c2213d31ed))
* Support extra plugin_args for php bazel rules rules ([#3165](https://www.github.com/googleapis/rules_gapic/issues/3165)) ([1f91fbb](https://www.github.com/googleapis/rules_gapic/commit/1f91fbb40cf524763d79fc6e38f049266d773f3c))


### Bug Fixes

* Add javax dependency for `java.annotations` bazel targets. ([#3155](https://www.github.com/googleapis/rules_gapic/issues/3155)) ([6c01035](https://www.github.com/googleapis/rules_gapic/commit/6c01035cc7ad8a661c19fd0e8d7fffbeaa542c12))
* allow PHP builds to occur without iconv dependency ([#3154](https://www.github.com/googleapis/rules_gapic/issues/3154)) ([dcd5022](https://www.github.com/googleapis/rules_gapic/commit/dcd50226ae262b8e3bca7366e8780d5cf324f8ce))
* build_gen use lro gapic as lro dep ([#3215](https://www.github.com/googleapis/rules_gapic/issues/3215)) ([fab708c](https://www.github.com/googleapis/rules_gapic/commit/fab708cf290ffd9529535ec70df1c3950bdc0d88))
* **build_gen:** include httpbody import for java/go ([#3329](https://www.github.com/googleapis/rules_gapic/issues/3329)) ([f3dedfb](https://www.github.com/googleapis/rules_gapic/commit/f3dedfbe56329643da86f4ac765f44e7a17ae8ce))
* Fix bazel for discogapic ([#3152](https://www.github.com/googleapis/rules_gapic/issues/3152)) ([6c3518f](https://www.github.com/googleapis/rules_gapic/commit/6c3518f6d9f141317ed7ecad08fb1e7940ce3af6))
* Fix BUILD file generator (accept dashes in proto options) ([#3187](https://www.github.com/googleapis/rules_gapic/issues/3187)) ([879ad64](https://www.github.com/googleapis/rules_gapic/commit/879ad648c0de5ec86044edec8a23f0032cc04977))
* Fix for java_gapic_assembly_gradle_pkg rule. ([#3253](https://www.github.com/googleapis/rules_gapic/issues/3253)) ([358fdc1](https://www.github.com/googleapis/rules_gapic/commit/358fdc1b3bfca483e811187a92efde217d6605cd))
* Fix tar packaging in bazel rules for mac ([#3255](https://www.github.com/googleapis/rules_gapic/issues/3255)) ([3da3bf1](https://www.github.com/googleapis/rules_gapic/commit/3da3bf1516827569b622e4ee5ac3a3a66f2f7919))
* ignore grpc_service_config unless using gapic_v2 ([d433cc0](https://www.github.com/googleapis/rules_gapic/commit/d433cc0b5cce206d9f29803c4ff9b9de0a64f0de))
* nodejs build_gen uses grpc_service_config ([#3206](https://www.github.com/googleapis/rules_gapic/issues/3206)) ([d433cc0](https://www.github.com/googleapis/rules_gapic/commit/d433cc0b5cce206d9f29803c4ff9b9de0a64f0de))
* preserve the value of opt_args parameter ([#3319](https://www.github.com/googleapis/rules_gapic/issues/3319)) ([f9cf185](https://www.github.com/googleapis/rules_gapic/commit/f9cf18543a880893821c4a2b93bebdb6b5313562))
* **PYTHON:** update nox file and modernize to new interface ([#3156](https://www.github.com/googleapis/rules_gapic/issues/3156)) ([f3e5a70](https://www.github.com/googleapis/rules_gapic/commit/f3e5a70bb5f04a63f891a740cad01d95bd00822d))
* **python:** use '\' for string escapes ([#3335](https://www.github.com/googleapis/rules_gapic/issues/3335)) ([92a3652](https://www.github.com/googleapis/rules_gapic/commit/92a3652776d568ed6472c35e4f9aa72c854a3780))
* remove unused package option from java_gapic_library [gapic-generator] ([#3341](https://www.github.com/googleapis/rules_gapic/issues/3341)) ([854d94b](https://www.github.com/googleapis/rules_gapic/commit/854d94bd055d7a1ae0becf11f1cfd313ecf352fc))
* use None instead of "" when grpc_service_config is not present ([d433cc0](https://www.github.com/googleapis/rules_gapic/commit/d433cc0b5cce206d9f29803c4ff9b9de0a64f0de))
* use remote lro Go gapic instead of local ([#3299](https://www.github.com/googleapis/rules_gapic/issues/3299)) ([9efefe3](https://www.github.com/googleapis/rules_gapic/commit/9efefe38a655041fd165de592af726d9b9b39229))
