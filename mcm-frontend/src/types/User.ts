import { QueryDashboard } from "@/types/Dashboard.ts";

export type UserRole = {
  name: string;
  permissions: string[];
};

export type User = {
  username: string;
  roles: UserRole[];
  privateDashboards: QueryDashboard[];
};

export type NewUser = {
  username: string;
  password: string;
  roles: UserRole[];
  privateDashboards?: QueryDashboard[];
};
