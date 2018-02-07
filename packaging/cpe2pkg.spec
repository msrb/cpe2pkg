Name:           cpe2pkg
Version:        1
Release:        1%{?dist}
Summary:        Guess package names with easy
License:        ASL 2.0
URL:            https://github.com/msrb/cpe2pkg
Source0:        %{name}.jar

BuildArch:      noarch

BuildRequires:  javapackages-tools


%description
Guessing package names since 2018.

%prep

%install
install -p -m 644 %{SOURCE0} %{buildroot}%{_javadir}/%{name}.jar

%jpackage_script  "" "" %{name} %{name} true

%files
%{_javadir}/%{name}.jar
%{_bindir}/%{name}

%changelog
* Tue Feb 06 2018 Michal Srb <michal@redhat.com> - 1-1
- Initial packaging

