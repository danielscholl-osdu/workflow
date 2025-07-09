# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.0.0 (2025-07-09)


### ✨ Features

* Using keda scaling ([4a179bf](https://github.com/danielscholl-osdu/workflow/commit/4a179bfb60c8fc824f313e555a5f595ee1b1963e))


### 🐛 Bug Fixes

* Adding commons-io ([745c22c](https://github.com/danielscholl-osdu/workflow/commit/745c22c443a7389b838062394cd27da37dfe1b33))
* Adding commons-io ([f2cd79f](https://github.com/danielscholl-osdu/workflow/commit/f2cd79fd250e2b7b4c81567165a2086be02cc836))
* Code smells ([dc0006f](https://github.com/danielscholl-osdu/workflow/commit/dc0006ff95c01b4a8c513512853e252fb97726fe))
* Moving test scripts in the buildspec commands ([279ffbe](https://github.com/danielscholl-osdu/workflow/commit/279ffbe208d8821fc5ca07d2299168972abb4061))
* Spring cves ([b69f1a8](https://github.com/danielscholl-osdu/workflow/commit/b69f1a8ff70f1ffd4873cbc5e2dd2f537e3e5e4e))
* Spring cves ([b326712](https://github.com/danielscholl-osdu/workflow/commit/b326712d97452577e3144733962f45d1cea1921a))
* Spring cves ([7a63826](https://github.com/danielscholl-osdu/workflow/commit/7a6382692aa7bd3ee7f139bebbfb9c68324243de))
* Tomcat core CVE ([19874b5](https://github.com/danielscholl-osdu/workflow/commit/19874b500a8b3030ab800e49a1096d416a2db9a1))
* Tomcat core CVE ([4fcaadd](https://github.com/danielscholl-osdu/workflow/commit/4fcaaddd9ea4812d622a370f75d5931830a29203))
* Update replicas ([a73a2da](https://github.com/danielscholl-osdu/workflow/commit/a73a2dad8dff6bc5ed413be99f011b1b6694ea6a))
* Update replicas ([094464d](https://github.com/danielscholl-osdu/workflow/commit/094464d827c12b3eaed0a34b6f47da7c20d7fb84))
* Use parent.version instead of project.version ([acf6830](https://github.com/danielscholl-osdu/workflow/commit/acf6830dac139a5448ce8d9209c0274b59f95c04))
* Use parent.version instead of project.version ([97d10b2](https://github.com/danielscholl-osdu/workflow/commit/97d10b245759c9d8cbafbda514f723f894243b8d))


### 📚 Documentation

* Updating helm chart documentation and versioning ([61e2897](https://github.com/danielscholl-osdu/workflow/commit/61e2897df45fb7cdc99d13a78f8411d53b1d0fac))


### 🔧 Miscellaneous

* Complete repository initialization ([f7f7853](https://github.com/danielscholl-osdu/workflow/commit/f7f7853d9472c75558835b7ae4a0b3ff7219e56c))
* Copy configuration and workflows from main branch ([531e517](https://github.com/danielscholl-osdu/workflow/commit/531e517f094f39d89429d55ae864628db0882a19))
* Deleting aws helm chart ([a0d9931](https://github.com/danielscholl-osdu/workflow/commit/a0d993135b91026a803cccd2cf7a3093ac2bdceb))
* Deleting aws helm chart ([80bac49](https://github.com/danielscholl-osdu/workflow/commit/80bac49106768d37595859929578afe2d5490dd4))
* Removing helm copy from aws buildspec ([4c169a4](https://github.com/danielscholl-osdu/workflow/commit/4c169a4f56aae6fb700eedccc72f87d7b0e3c5b0))


### ⚙️ Continuous Integration

* Add bootstrap image job ([db89c43](https://github.com/danielscholl-osdu/workflow/commit/db89c431e909a081892122c8e9e0cf5cb9b39f80))
* Add variable for tests ([0ff4a53](https://github.com/danielscholl-osdu/workflow/commit/0ff4a53a7f1ac62f0e3a6400a110a6ab74842e8b))
* Fix chart name ([5ac8899](https://github.com/danielscholl-osdu/workflow/commit/5ac88999195ebc6e0cc1995b39884de5135f0d0b))
* Fix chart templates ([2d3f87c](https://github.com/danielscholl-osdu/workflow/commit/2d3f87cdd9e23b42e667b6e198facc45222b5bb6))
* Temporary disable other providers ([5329c39](https://github.com/danielscholl-osdu/workflow/commit/5329c39163a742a38ffd1cb1b441eeadd27dab5c))

## [2.0.0] - Major Workflow Enhancement & Documentation Release

### ✨ Features
- **Comprehensive MkDocs Documentation Site**: Complete documentation overhaul with GitHub Pages deployment
- **Automated Cascade Failure Recovery**: System automatically recovers from cascade workflow failures
- **Human-Centric Cascade Pattern**: Issue lifecycle tracking with human notifications for critical decisions
- **Integration Validation**: Comprehensive validation system for cascade workflows
- **Claude Workflow Integration**: Full Claude Code CLI support with Maven MCP server integration
- **GitHub Copilot Enhancement**: Java development environment setup and firewall configuration
- **Fork Resources Staging Pattern**: Template-based staging for fork-specific configurations
- **Conventional Commits Validation**: Complete validation system with all supported commit types
- **Enhanced PR Label Management**: Simplified production PR labels with automated issue closure
- **Meta Commit Strategy**: Advanced release-please integration for better version management
- **Push Protection Handling**: Sophisticated upstream secrets detection and resolution workflows

### 🔨 Build System
- **Workflow Separation Pattern**: Template development vs. fork instance workflow isolation
- **Template Workflow Management**: 9 comprehensive template workflows for fork management
- **Enhanced Action Reliability**: Improved cascade workflow trigger reliability with PR event filtering
- **Base64 Support**: Enhanced create-enhanced-pr action with encoding capabilities

### 📚 Documentation
- **Structured MkDocs Site**: Complete documentation architecture with GitHub Pages
- **AI-First Development Docs**: Comprehensive guides for AI-enhanced development
- **ADR Documentation**: 20+ Architectural Decision Records covering all major decisions
- **Workflow Specifications**: Detailed documentation for all 9 template workflows
- **Streamlined README**: Focused quick-start guide directing to comprehensive documentation

### 🛡️ Security & Reliability
- **Advanced Push Protection**: Intelligent handling of upstream repositories with secrets
- **Branch Protection Integration**: Automated branch protection rule management
- **Security Pattern Recognition**: Enhanced security scanning and pattern detection
- **MCP Configuration**: Secure Model Context Protocol integration for AI development

### 🔧 Workflow Enhancements
- **Cascade Monitoring**: Advanced cascade workflow monitoring and SLA management
- **Dependabot Integration**: Enhanced dependabot validation and automation
- **Template Synchronization**: Sophisticated template update propagation system
- **Issue State Tracking**: Advanced issue lifecycle management and tracking
- **GITHUB_TOKEN Standardization**: Improved token handling across all workflows

### ♻️ Code Refactoring
- **Removed AI_EVOLUTION.md**: Migrated to structured ADR approach for better maintainability
- **Simplified README Structure**: Eliminated redundancy between README and documentation site
- **Enhanced Initialization Cleanup**: Improved fork repository cleanup and setup process
- **Standardized Error Handling**: Consistent error handling patterns across all workflows

### 🐛 Bug Fixes
- **YAML Syntax Issues**: Resolved multiline string handling in workflow configurations
- **Release Workflow Compatibility**: Updated to googleapis/release-please-action@v4
- **MCP Server Configuration**: Fixed Maven MCP server connection and configuration issues
- **Cascade Trigger Reliability**: Implemented pull_request_target pattern for better triggering
- **Git Diff Syntax**: Corrected git command syntax in sync-template workflow
- **Label Management**: Standardized label usage across all workflows and templates

## [1.0.0] - Initial Release

### ✨ Features
- Initial release of OSDU Fork Management Template
- Automated fork initialization workflow
- Daily upstream synchronization with AI-enhanced PR descriptions
- Three-branch management strategy (main, fork_upstream, fork_integration)
- Automated conflict detection and resolution guidance
- Semantic versioning and release management
- Template development workflows separation

### 📚 Documentation
- Complete architectural decision records (ADRs)
- Product requirements documentation
- Development and usage guides
- GitHub Actions workflow documentation
