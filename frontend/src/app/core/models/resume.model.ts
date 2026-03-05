export interface ResumeSection {
  id: number;
  section: string;
  sortOrder: number;
  title: string;
  subtitle: string | null;
  location: string | null;
  startDate: string | null;
  endDate: string | null;
  description: string | null;
  isVisible: boolean;
  updatedAt?: string;
}



