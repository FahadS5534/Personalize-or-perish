import { Component, OnInit, inject, signal, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { 
  Assessment, 
  Question, 
  SubmissionResponse, 
  LearningGap, 
  StudyPlan, 
  StudentDashboardAnalytics,
  AnswerRequest,
  ChatResponse
} from '../../core/models/models';

interface ChatBubble {
  sender: 'student' | 'bot';
  text: string;
  sources?: string[];
}

@Component({
  selector: 'app-student',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './student.component.html',
  styleUrls: ['./student.component.css']
})
export class StudentComponent implements OnInit, AfterViewChecked {
  private authService = inject(AuthService);
  private http = inject(HttpClient);
  private router = inject(Router);

  @ViewChild('chatScrollContainer') private chatScrollContainer!: ElementRef;

  // Signalled auth details
  currentUser = this.authService.currentUser;

  // UI State
  activeTab = 'dashboard';
  isLoading = false;

  // Data fields
  learningGaps: LearningGap[] = [];
  assessments: Assessment[] = [];
  studentAnalytics: StudentDashboardAnalytics | null = null;
  currentStudyPlan: StudyPlan | null = null;

  // Active quiz runner
  activeAssessment: Assessment | null = null;
  activeQuestions: Question[] = [];
  currentQuestionIndex = 0;
  selectedAnswers: { [questionId: number]: 'A' | 'B' | 'C' | 'D' } = {};
  isSubmitting = false;
  quizResult: SubmissionResponse | null = null;

  // Study plan actions
  isGeneratingPlan = false;

  // Tutor Chat
  chatInput = '';
  chatHistory: ChatBubble[] = [];
  isChatLoading = false;

  ngOnInit() {
    const student = this.currentUser();
    if (!student) {
      this.router.navigate(['/auth/login']);
      return;
    }

    this.loadDashboardData();
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  private loadDashboardData() {
    const studentId = this.currentUser()?.id;
    if (!studentId) return;

    this.isLoading = true;

    // 1. Fetch Analytics
    this.http.get<StudentDashboardAnalytics>(`http://localhost:8080/api/analytics/student/${studentId}`)
      .subscribe({
        next: (data) => this.studentAnalytics = data,
        error: (err) => console.error('Failed to load analytics', err)
      });

    // 2. Fetch Learning Gaps
    this.http.get<LearningGap[]>(`http://localhost:8080/api/learning-gaps/student/${studentId}`)
      .subscribe({
        next: (data) => this.learningGaps = data,
        error: (err) => console.error('Failed to load gaps', err)
      });

    // 3. Fetch Assessments
    this.http.get<Assessment[]>(`http://localhost:8080/api/assessments`)
      .subscribe({
        next: (data) => this.assessments = data,
        error: (err) => console.error('Failed to load assessments', err)
      });

    // 4. Fetch Latest Study Plan
    this.http.get<StudyPlan>(`http://localhost:8080/api/study-plans/student/${studentId}/latest`)
      .subscribe({
        next: (data) => this.currentStudyPlan = data,
        error: (err) => {
          this.currentStudyPlan = null;
        }
      });
  }

  getAccuracyClass(accuracy: number): string {
    if (accuracy >= 80) return 'progress-high';
    if (accuracy >= 50) return 'progress-med';
    return 'progress-low';
  }

  // Quiz Navigation
  startAssessment(assessment: Assessment) {
    this.activeAssessment = assessment;
    this.currentQuestionIndex = 0;
    this.selectedAnswers = {};
    this.quizResult = null;
    this.activeQuestions = [];

    this.http.get<Question[]>(`http://localhost:8080/api/assessments/${assessment.id}/questions`)
      .subscribe({
        next: (data) => {
          this.activeQuestions = data;
        },
        error: (err) => console.error('Failed to start quiz', err)
      });
  }

  getCurrentQuestion(): Question | null {
    if (this.activeQuestions.length === 0) return null;
    return this.activeQuestions[this.currentQuestionIndex] || null;
  }

  prevQuestion() {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
    }
  }

  nextQuestion() {
    if (this.currentQuestionIndex < this.activeQuestions.length - 1) {
      this.currentQuestionIndex++;
    }
  }

  submitQuiz() {
    if (!this.activeAssessment) return;

    this.isSubmitting = true;
    const answersList: AnswerRequest[] = [];

    Object.keys(this.selectedAnswers).forEach((qIdStr) => {
      const qId = parseInt(qIdStr, 10);
      answersList.push({
        questionId: qId,
        selectedAnswer: this.selectedAnswers[qId]
      });
    });

    this.http.post<SubmissionResponse>(`http://localhost:8080/api/submissions`, {
      assessmentId: this.activeAssessment.id,
      answers: answersList
    }).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.quizResult = res;
        this.loadDashboardData(); // Refresh gaps in dashboard
      },
      error: (err) => {
        this.isSubmitting = false;
        alert('Failed to submit quiz: ' + (err.error?.message || err.message));
      }
    });
  }

  cancelQuiz() {
    this.activeAssessment = null;
    this.activeQuestions = [];
    this.currentQuestionIndex = 0;
    this.selectedAnswers = {};
  }

  closeResults() {
    this.activeAssessment = null;
    this.activeQuestions = [];
    this.quizResult = null;
    this.activeTab = 'dashboard';
  }

  // Study Plan Actions
  generateStudyPlan() {
    const studentId = this.currentUser()?.id;
    if (!studentId) return;

    this.isGeneratingPlan = true;
    this.http.post<StudyPlan>(`http://localhost:8080/api/study-plans/generate/${studentId}`, {})
      .subscribe({
        next: (data) => {
          this.isGeneratingPlan = false;
          this.currentStudyPlan = data;
        },
        error: (err) => {
          this.isGeneratingPlan = false;
          alert('Failed to generate plan: ' + (err.error?.message || 'Check if you have learning gaps first!'));
        }
      });
  }

  togglePlanItem(itemId: number, event: any) {
    if (!this.currentStudyPlan) return;

    const completed = event.target.checked;
    this.http.put<StudyPlan>(`http://localhost:8080/api/study-plans/${this.currentStudyPlan.id}/items/${itemId}?completed=${completed}`, {})
      .subscribe({
        next: (data) => {
          this.currentStudyPlan = data;
        },
        error: (err) => {
          console.error('Failed to toggle completion status', err);
          event.target.checked = !completed; // Revert checkbox
        }
      });
  }

  // Tutor Chat
  sendChatMessage() {
    if (!this.chatInput.trim()) return;

    const message = this.chatInput;
    this.chatHistory.push({ sender: 'student', text: message });
    this.chatInput = '';
    this.isChatLoading = true;

    let topicId: number | undefined = undefined;
    if (this.learningGaps.length > 0) {
      topicId = this.learningGaps[0].topicId;
    }

    this.http.post<ChatResponse>(`http://localhost:8080/api/tutor/chat`, {
      message: message,
      topicId: topicId
    }).subscribe({
      next: (res) => {
        this.isChatLoading = false;
        this.chatHistory.push({
          sender: 'bot',
          text: res.reply,
          sources: res.sources
        });
      },
      error: (err) => {
        this.isChatLoading = false;
        this.chatHistory.push({
          sender: 'bot',
          text: 'Sorry, I encountered an issue querying the course syllabus documents. Please try again shortly.'
        });
      }
    });
  }

  private scrollToBottom(): void {
    try {
      this.chatScrollContainer.nativeElement.scrollTop = this.chatScrollContainer.nativeElement.scrollHeight;
    } catch(err) { }
  }

  logout() {
    this.authService.logout();
  }
}
