import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { ResumeSection } from '../../core/models/resume.model';
import { environment } from '../../../environments/environment';
import { RichTextEditorComponent } from '../../shared/components/rich-text-editor/rich-text-editor.component';

@Component({
  selector: 'app-resume',
  standalone: true,
  imports: [FormsModule, NgClass, RichTextEditorComponent],
  templateUrl: './resume.component.html',
  styleUrl: './resume.component.css'
})
export class ResumeComponent implements OnInit {
  sections  = signal<ResumeSection[]>([]);
  loading   = signal(true);
  error     = signal('');
  saving    = signal(false);
  saveMsg   = signal('');

  editingId = signal<number | null>(null);
  draft     = signal<ResumeSection | null>(null);

  // ── Add modal ──
  showAddModal = signal(false);
  newEntry: Partial<ResumeSection> = {};

  // ── Drag state ──
  dragId: number | null = null;
  dragOverId: number | null = null;

  readonly SECTION_KEYS = ['SUMMARY','SKILLS','EXPERIENCE','EDUCATION','CERTIFICATIONS','OTHER'];

  constructor(private api: ApiService, protected auth: AuthService) {}

  get isAdmin() { return this.auth.isAdmin(); }

  // ordered list of distinct group names
  get groups(): string[] {
    const order = ['SUMMARY','SKILLS','EXPERIENCE','EDUCATION','CERTIFICATIONS','OTHER'];
    const present = [...new Set(this.sections().map(s => s.section))];
    return order.filter(g => present.includes(g))
                .concat(present.filter(g => !order.includes(g)));
  }

  itemsFor(group: string): ResumeSection[] {
    return this.sections()
      .filter(s => s.section === group)
      .sort((a, b) => a.sortOrder - b.sortOrder);
  }

  ngOnInit() {
    const call = this.isAdmin ? this.api.getAllResumeSections() : this.api.getResume();
    call.subscribe({
      next: s  => { this.sections.set(s); this.loading.set(false); },
      error: () => {
        // If admin call failed (e.g. stale token), fall back to public endpoint
        this.api.getResume().subscribe({
          next: s  => { this.sections.set(s); this.loading.set(false); },
          error: () => { this.error.set('Failed to load resume.'); this.loading.set(false); }
        });
      }
    });
  }

  // ── Admin editing ──────────────────────────────────────────
  startEdit(s: ResumeSection) {
    this.editingId.set(s.id);
    this.draft.set({ ...s });   // shallow copy so we edit the draft, not live data
  }

  cancelEdit() {
    this.editingId.set(null);
    this.draft.set(null);
  }

  saveEdit() {
    const d = this.draft();
    if (!d) return;
    this.saving.set(true);
    this.api.updateResumeSection(d.id, d).subscribe({
      next: updated => {
        this.sections.update(list => list.map(s => s.id === updated.id ? updated : s));
        this.editingId.set(null);
        this.draft.set(null);
        this.saving.set(false);
        this.saveMsg.set('Saved!');
        setTimeout(() => this.saveMsg.set(''), 2000);
      },
      error: () => { this.saving.set(false); this.saveMsg.set('Save failed.'); }
    });
  }

  deleteSection(id: number) {
    if (!confirm('Delete this entry?')) return;
    this.api.deleteResumeSection(id).subscribe({
      next: () => this.sections.update(list => list.filter(s => s.id !== id))
    });
  }

  // ── Add modal ────────────────────────────────────────────────
  openAddModal(defaultSection = 'EXPERIENCE') {
    const maxOrder = Math.max(0, ...this.sections()
      .filter(s => s.section === defaultSection)
      .map(s => s.sortOrder));
    this.newEntry = {
      section: defaultSection,
      title: '',
      subtitle: '',
      location: '',
      description: '',
      isVisible: true,
      sortOrder: maxOrder + 1
    };
    this.showAddModal.set(true);
  }

  closeAddModal() { this.showAddModal.set(false); }

  submitAdd() {
    if (!this.newEntry.title?.trim()) return;
    this.api.createResumeSection(this.newEntry).subscribe({
      next: created => {
        this.sections.update(list => [...list, created]);
        this.showAddModal.set(false);
        this.flash('✔ Entry added!');
      },
      error: () => this.flash('Failed to add entry.', true)
    });
  }

  // ── Drag & drop ──────────────────────────────────────────────
  onDragStart(id: number) { this.dragId = id; }
  onDragEnd()             { this.dragId = null; this.dragOverId = null; }
  onDragOver(id: number, e: DragEvent) { e.preventDefault(); this.dragOverId = id; }
  onDragLeave()           { this.dragOverId = null; }

  onDrop(targetId: number, group: string) {
    if (this.dragId === null || this.dragId === targetId) {
      this.dragId = null; this.dragOverId = null; return;
    }
    const items = this.itemsFor(group);
    const fromIdx = items.findIndex(s => s.id === this.dragId);
    const toIdx   = items.findIndex(s => s.id === targetId);
    if (fromIdx === -1 || toIdx === -1) { this.dragId = null; this.dragOverId = null; return; }

    const reordered = [...items];
    const [moved] = reordered.splice(fromIdx, 1);
    reordered.splice(toIdx, 0, moved);
    const updates = reordered.map((s, i) => ({ ...s, sortOrder: i }));

    // optimistic update
    this.sections.update(all => [
      ...all.filter(s => s.section !== group),
      ...updates
    ]);

    this.api.reorderResumeSections(updates.map(s => ({ id: s.id, sortOrder: s.sortOrder }))).subscribe({
      error: () => this.flash('Reorder failed.', true)
    });
    this.dragId = null; this.dragOverId = null;
  }

  // ── Helpers ──────────────────────────────────────────────────
  private flash(msg: string, isErr = false) {
    this.saveMsg.set(msg);
    setTimeout(() => this.saveMsg.set(''), 2500);
  }

  /** Auto-expands textarea as user types */
  autoResize(event: Event) {
    const el = event.target as HTMLTextAreaElement;
    el.style.height = 'auto';
    el.style.height = el.scrollHeight + 'px';
  }

  formatDate(d: string | null): string {
    if (!d) return 'Present';
    const date = new Date(d);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short' });
  }

  labelFor(group: string): string {
    const map: Record<string,string> = {
      SUMMARY: 'About',
      SKILLS: 'Skills',
      EXPERIENCE: 'Experience',
      EDUCATION: 'Education',
      CERTIFICATIONS: 'Certifications',
      OTHER: 'Other Experience'
    };
    return map[group] ?? group;
  }

  downloadResume() {
    const backendRoot = environment.apiUrl.replace(/\/api$/, '');
    window.open(`${backendRoot}/assets/resume.docx`, '_blank');
  }
}
