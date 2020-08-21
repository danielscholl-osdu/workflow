package org.opengroup.osdu.workflow.aws.util;

import java.util.Date;

// This class makes testing easier so that we can mock the return value its method
// For example, this is being used in WorkflowStatusRepositoryImplTest
public class DateTime {
    public Date getCurrentDate() {
        return new Date();
    }
}