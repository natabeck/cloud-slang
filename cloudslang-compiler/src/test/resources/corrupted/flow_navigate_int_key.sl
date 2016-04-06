#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang

flow:
 name: flow_navigate_int_key

 workflow:
   - Task1:
       do:
         ops.java_op:
       navigate:
         - 123: SUCCESS
         - FAILURE: FAILURE
 results:
  - SUCCESS
  - FAILURE