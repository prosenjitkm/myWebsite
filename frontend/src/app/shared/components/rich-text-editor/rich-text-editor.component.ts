import {
  Component,
  ElementRef,
  Input,
  OnDestroy,
  OnInit,
  ViewChild,
  forwardRef,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Editor } from '@tiptap/core';
import Document from '@tiptap/extension-document';
import Paragraph from '@tiptap/extension-paragraph';
import Text from '@tiptap/extension-text';
import Bold from '@tiptap/extension-bold';
import Italic from '@tiptap/extension-italic';
import Underline from '@tiptap/extension-underline';
import BulletList from '@tiptap/extension-bullet-list';
import OrderedList from '@tiptap/extension-ordered-list';
import ListItem from '@tiptap/extension-list-item';
import TextAlign from '@tiptap/extension-text-align';
import Heading from '@tiptap/extension-heading';
import History from '@tiptap/extension-history';
import HardBreak from '@tiptap/extension-hard-break';

@Component({
  selector: 'app-rich-text-editor',
  standalone: true,
  imports: [],
  templateUrl: './rich-text-editor.component.html',
  styleUrl: './rich-text-editor.component.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => RichTextEditorComponent),
      multi: true,
    },
  ],
})
export class RichTextEditorComponent implements OnInit, OnDestroy, ControlValueAccessor {
  @Input() placeholder = 'Start typing…';
  @ViewChild('editorEl', { static: true }) editorEl!: ElementRef<HTMLDivElement>;

  editor!: Editor;

  private onChange: (v: string) => void = () => {};
  private onTouched: () => void = () => {};

  ngOnInit() {
    this.editor = new Editor({
      element: this.editorEl.nativeElement,
      extensions: [
        Document,
        Paragraph,
        Text,
        Bold,
        Italic,
        Underline,
        BulletList,
        OrderedList,
        ListItem,
        HardBreak,
        Heading.configure({ levels: [1, 2, 3] }),
        TextAlign.configure({ types: ['heading', 'paragraph'] }),
        History,
      ],
      content: '',
      onUpdate: ({ editor }) => {
        const html = editor.getHTML();
        this.onChange(html === '<p></p>' ? '' : html);
      },
      onBlur: () => this.onTouched(),
    });
  }

  ngOnDestroy() {
    this.editor?.destroy();
  }

  // ControlValueAccessor
  writeValue(value: string): void {
    if (this.editor && value !== this.editor.getHTML()) {
      this.editor.commands.setContent(value || '');
    }
  }
  registerOnChange(fn: (v: string) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(disabled: boolean): void {
    disabled ? this.editor?.setEditable(false) : this.editor?.setEditable(true);
  }

  // Toolbar helpers
  isActive(name: string, attrs?: Record<string, unknown>): boolean {
    return this.editor?.isActive(name, attrs) ?? false;
  }

  isAlignActive(dir: string): boolean {
    return this.editor?.isActive({ textAlign: dir }) ?? false;
  }

  toggle(command: string) {
    const c = this.editor?.chain().focus();
    switch (command) {
      case 'bold':          c.toggleBold().run(); break;
      case 'italic':        c.toggleItalic().run(); break;
      case 'underline':     c.toggleUnderline().run(); break;
      case 'bulletList':    c.toggleBulletList().run(); break;
      case 'orderedList':   c.toggleOrderedList().run(); break;
      case 'h2':            c.toggleHeading({ level: 2 }).run(); break;
      case 'h3':            c.toggleHeading({ level: 3 }).run(); break;
    }
  }

  align(direction: 'left' | 'center' | 'right' | 'justify') {
    this.editor?.chain().focus().setTextAlign(direction).run();
  }

  undo() { this.editor?.chain().focus().undo().run(); }
  redo() { this.editor?.chain().focus().redo().run(); }
}




