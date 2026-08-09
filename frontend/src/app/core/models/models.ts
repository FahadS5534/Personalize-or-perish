export interface User {
  id: number;
  name: string;
  email: string;
  role: 'STUDENT' | 'FACILITATOR' | 'ADMIN';
  token?: string;
}

export interface Topic {
  id: number;
  name: string;
}

export interface Question {
  id?: number;
  questionText: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  correctAnswer: 'A' | 'B' | 'C' | 'D';
  topicId?: number;
  topicName?: string;
  difficulty?: 'EASY' | 'MEDIUM' | 'HARD';
}

export interface Assessment {
  id: number;
  title: string;
  description: string;
}

export interface AnswerRequest {
  questionId: number;
  selectedAnswer: 'A' | 'B' | 'C' | 'D';
}

export interface SubmissionRequest {
  assessmentId: number;
  answers: AnswerRequest[];
}

export interface SubmissionResponse {
  id: number;
  assessmentId: number;
  assessmentTitle: string;
  studentName: string;
  score: number;
  totalQuestions: number;
  percentage: number;
  submittedAt: string;
}

export interface LearningGap {
  id: number;
  topicId: number;
  topicName: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH';
  accuracy: number;
  explanation: string;
  identifiedAt: string;
}

export interface StudyPlanItem {
  id: number;
  topicId: number;
  topicName: string;
  content: string;
  estimatedMinutes: number;
  priority: string;
  completed: boolean;
}

export interface StudyPlan {
  id: number;
  studentId: number;
  studentName: string;
  title: string;
  description: string;
  createdAt: string;
  items: StudyPlanItem[];
}

export interface DocumentResponse {
  id: number;
  filename: string;
  title: string;
  topicId: number;
  topicName: string;
  uploadedById: number;
  uploadedByName: string;
  uploadedAt: string;
}

export interface StudyMaterial {
  id: number;
  topicId: number;
  topicName: string;
  title: string;
  content: string;
  type: 'GENERATED' | 'MANUAL';
}

export interface ChatResponse {
  reply: string;
  sources: string[];
}

export interface StudentDashboardAnalytics {
  averageScore: number;
  totalSubmissions: number;
  topicPerformance: { [key: string]: number };
  weakTopics: string[];
  strongTopics: string[];
  recentSubmissions: SubmissionResponse[];
}

export interface TopicPerformance {
  topic: string;
  accuracy: number;
}

export interface DifficultQuestion {
  questionId?: number;
  questionText: string;
  failureRate: number;
}

export interface CommonLearningGap {
  topic: string;
  count: number;
}

export interface FacilitatorAnalytics {
  averageScore: number;
  highestScore: number;
  lowestScore: number;
  topicPerformance: TopicPerformance[];
  numberOfStudents: number;
  difficultQuestions: DifficultQuestion[];
  commonLearningGaps: CommonLearningGap[];
}
