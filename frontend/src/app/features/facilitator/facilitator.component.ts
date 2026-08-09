import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { 
  Topic, 
  Assessment, 
  Question, 
  DocumentResponse, 
  StudyMaterial, 
  FacilitatorAnalytics 
} from '../../core/models/models';

@Component({
  selector: 'app-facilitator',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './facilitator.component.html',
  styleUrls: ['./facilitator.component.css']
})
export class FacilitatorComponent implements OnInit {
  private authService = inject(AuthService);
  private http = inject(HttpClient);
  private router = inject(Router);

  // Signalled user details
  currentUser = this.authService.currentUser;

  // View tabs
  activeTab = 'analytics';

  // Academic lists
  topics: Topic[] = [];
  assessments: Assessment[] = [];
  documents: DocumentResponse[] = [];
  studyMaterials: StudyMaterial[] = [];

  // Selection
  selectedAssessment: Assessment | null = null;
  selectedAssessmentQuestions: Question[] = [];
  facilitatorAnalytics: FacilitatorAnalytics | null = null;

  // Form toggles
  showCreateQuizForm = false;

  // Form states: New Quiz
  newQuizTitle = '';
  newQuizDesc = '';

  // Form states: New Question
  newQTopicId: number | null = null;
  newQText = '';
  newQOptionA = '';
  newQOptionB = '';
  newQOptionC = '';
  newQOptionD = '';
  newQCorrect: 'A' | 'B' | 'C' | 'D' = 'A';

  // Form states: Document Upload
  uploadTopicId: number | null = null;
  selectedFile: File | null = null;
  isUploadingDoc = false;

  // Form states: Material Generator
  generateTopicId: number | null = null;
  generateDifficulty: 'EASY' | 'MEDIUM' | 'HARD' = 'MEDIUM';
  isGeneratingGuide = false;

  ngOnInit() {
    if (!this.currentUser()) {
      this.router.navigate(['/auth/login']);
      return;
    }

    this.loadTopics();
    this.loadAssessments();
    this.loadDocuments();
    this.loadStudyMaterials();
  }

  // Loaders
  private loadTopics() {
    this.http.get<Topic[]>('http://localhost:8080/api/topics')
      .subscribe({
        next: (data) => {
          this.topics = data;
          if (data.length > 0) {
            this.newQTopicId = data[0].id;
            this.uploadTopicId = data[0].id;
            this.generateTopicId = data[0].id;
          }
        },
        error: (err) => console.error('Failed to load topics', err)
      });
  }

  private loadAssessments() {
    this.http.get<Assessment[]>('http://localhost:8080/api/assessments')
      .subscribe({
        next: (data) => {
          this.assessments = data;
          // Auto select first assessment to load default analytics if available
          if (data.length > 0 && !this.selectedAssessment) {
            this.selectAssessment(data[0]);
          }
        },
        error: (err) => console.error('Failed to load assessments', err)
      });
  }

  private loadDocuments() {
    this.http.get<DocumentResponse[]>('http://localhost:8080/api/documents')
      .subscribe({
        next: (data) => this.documents = data,
        error: (err) => console.error('Failed to load documents', err)
      });
  }

  private loadStudyMaterials() {
    this.http.get<StudyMaterial[]>('http://localhost:8080/api/study-materials')
      .subscribe({
        next: (data) => this.studyMaterials = data,
        error: (err) => console.error('Failed to load study materials', err)
      });
  }

  // Selection
  selectAssessment(assessment: Assessment) {
    this.selectedAssessment = assessment;
    this.selectedAssessmentQuestions = [];
    this.facilitatorAnalytics = null;
    this.showCreateQuizForm = false;

    // Load Questions
    this.http.get<Question[]>(`http://localhost:8080/api/assessments/${assessment.id}/questions`)
      .subscribe({
        next: (data) => this.selectedAssessmentQuestions = data,
        error: (err) => console.error('Failed to load assessment questions', err)
      });

    // Load Analytics
    this.http.get<FacilitatorAnalytics>(`http://localhost:8080/api/analytics/assessment/${assessment.id}`)
      .subscribe({
        next: (data) => this.facilitatorAnalytics = data,
        error: (err) => {
          // If no submissions exist, set to empty default metrics rather than crashing
          this.facilitatorAnalytics = {
            averageScore: 0,
            highestScore: 0,
            lowestScore: 0,
            numberOfStudents: 0,
            topicPerformance: [],
            difficultQuestions: [],
            commonLearningGaps: []
          };
        }
      });
  }

  // Quiz Management
  onCreateQuizSubmit() {
    if (!this.newQuizTitle) return;

    this.http.post<Assessment>('http://localhost:8080/api/assessments', {
      title: this.newQuizTitle,
      description: this.newQuizDesc
    }).subscribe({
      next: (res) => {
        this.newQuizTitle = '';
        this.newQuizDesc = '';
        this.showCreateQuizForm = false;
        this.loadAssessments();
        this.selectAssessment(res);
      },
      error: (err) => alert('Failed to create quiz: ' + err.message)
    });
  }

  deleteAssessment(id: number, event: MouseEvent) {
    event.stopPropagation(); // Stop selection trigger
    if (!confirm('Are you sure you want to delete this assessment?')) return;

    this.http.delete(`http://localhost:8080/api/assessments/${id}`)
      .subscribe({
        next: () => {
          if (this.selectedAssessment?.id === id) {
            this.selectedAssessment = null;
            this.selectedAssessmentQuestions = [];
            this.facilitatorAnalytics = null;
          }
          this.loadAssessments();
        },
        error: (err) => alert('Failed to delete assessment: ' + err.message)
      });
  }

  // Question Management
  onAddQuestionSubmit() {
    if (!this.selectedAssessment || !this.newQTopicId) return;

    this.http.post<Question>(`http://localhost:8080/api/assessments/${this.selectedAssessment.id}/questions`, {
      topicId: this.newQTopicId,
      questionText: this.newQText,
      optionA: this.newQOptionA,
      optionB: this.newQOptionB,
      optionC: this.newQOptionC,
      optionD: this.newQOptionD,
      correctAnswer: this.newQCorrect,
      difficulty: 'MEDIUM' // Default difficulty
    }).subscribe({
      next: () => {
        this.newQText = '';
        this.newQOptionA = '';
        this.newQOptionB = '';
        this.newQOptionC = '';
        this.newQOptionD = '';
        this.newQCorrect = 'A';
        if (this.selectedAssessment) {
          this.selectAssessment(this.selectedAssessment);
        }
      },
      error: (err) => alert('Failed to add question: ' + err.message)
    });
  }

  // Document Uploads
  onFileSelected(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.selectedFile = event.target.files[0];
    }
  }

  onUploadDocument() {
    if (!this.selectedFile || !this.uploadTopicId) return;

    this.isUploadingDoc = true;
    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('topicId', this.uploadTopicId.toString());

    this.http.post<DocumentResponse>('http://localhost:8080/api/documents/upload', formData)
      .subscribe({
        next: () => {
          this.isUploadingDoc = false;
          this.selectedFile = null;
          this.loadDocuments();
          alert('Document uploaded and vector indexed in ChromaDB successfully!');
        },
        error: (err) => {
          this.isUploadingDoc = false;
          alert('Upload failed: ' + (err.error?.message || err.message));
        }
      });
  }

  deleteDocument(id: number) {
    if (!confirm('Are you sure you want to delete this document context from vector index?')) return;

    this.http.delete(`http://localhost:8080/api/documents/${id}`)
      .subscribe({
        next: () => this.loadDocuments(),
        error: (err) => alert('Failed to delete document: ' + err.message)
      });
  }

  // Study Material Generator
  onGenerateMaterial() {
    if (!this.generateTopicId) return;

    this.isGeneratingGuide = true;
    this.http.post<StudyMaterial>('http://localhost:8080/api/study-materials/generate', {
      topicId: this.generateTopicId,
      difficulty: this.generateDifficulty
    }).subscribe({
      next: () => {
        this.isGeneratingGuide = false;
        this.loadStudyMaterials();
        alert('AI Study Guide generated and auto-indexed in vector storage successfully!');
      },
      error: (err) => {
        this.isGeneratingGuide = false;
        alert('Generation failed: ' + (err.error?.message || err.message));
      }
    });
  }

  logout() {
    this.authService.logout();
  }
}
