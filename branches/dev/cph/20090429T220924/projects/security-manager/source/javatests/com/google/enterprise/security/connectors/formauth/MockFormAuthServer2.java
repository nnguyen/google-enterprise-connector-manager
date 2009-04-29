// Copyright (C) 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.security.connectors.formauth;

import javax.servlet.http.Cookie;

public class MockFormAuthServer2 extends MockFormAuthServer {
  private static final long serialVersionUID = 1L;
  public MockFormAuthServer2() {
    super("username", "password");
    passwordMap.put("jim", "electrician");
    cookieMap.put("jim", new Cookie("Server2ID", "whassup"));
  }
}
// Copyright (C) 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.security.connectors.formauth;

import javax.servlet.http.Cookie;

public class MockFormAuthServer2 extends MockFormAuthServer {
  private static final long serialVersionUID = 1L;
  public MockFormAuthServer2() {
    super("username", "password");
    passwordMap.put("jim", "electrician");
    cookieMap.put("jim", new Cookie("Server2ID", "whassup"));
  }
}
